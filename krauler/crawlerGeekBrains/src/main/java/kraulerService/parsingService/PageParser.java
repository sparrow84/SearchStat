package kraulerService.parsingService;


import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import java.util.*;


public class PageParser {

    private static String WORK_FOLDER_PATH = "workFileStorage";

    private static int logLevel = 0;

    public static void setLogLevel(int logLevel) {
        PageParser.logLevel = logLevel;
    }

    public static int getLogLevel() {
        return logLevel;
    }

    public static String getWorkFolderPath() {
        return WORK_FOLDER_PATH;
    }


    public static void setWorkFolderPath(String workFolderPath) {
        WORK_FOLDER_PATH = workFolderPath;
    }


    /**
     * Поиск ссылок на Sitemap в robots.txt на вход подаётся строка
     * @param robots
     * @return Список ссылок на sitemap'ы
     */
    public static List<String> searchSiteMap(String robots) {

        String keyForSearch = "sitemap:";                   // ключ для поиска ссылки на saitmap
        String robotsLowerCase = robots.toLowerCase();      // приводим строку к нижнему регистру для упрощения поиска
        StringBuilder stringBuilder = new StringBuilder();  // стринг билдер для посимвольного построения найденной ссылки после keyForSearch
        // В строке ниже, среда разработки почемуто ругалась на невозможность использования даймонд в версии jdk1.5, хотя и в проекте указал версию 1.8
        // и далее по тексту кода ниже такиеже неприятности (например try with resorses)
        List<String> siteMapXmlLinks = new ArrayList<String>();   // результирующий список для вывода из метода найденных ссылок

        boolean keyFound = false;  // отметка о том что keyForSearch найден или нет
        char separator = ' ';      // разделитель строк в файле который был передан в этот метод в качестве одной строки

        for (int i = 0; i < robotsLowerCase.length(); i++) {         // цикл по символам переданной строки
            for (int j = 0; j < keyForSearch.length(); j++, i++) {   // цикл по символам keyForSearch

                if (!(robotsLowerCase.charAt(i) == keyForSearch.charAt(j))) {  // если символы не равны прерываем цикл по символам keyForSearch
                    keyFound = false;
                    break;
                } else {
                    keyFound = true;
                }
            }

            if (keyFound) { // если keyForSearch найден, берём ссылку и записываем её в список
                for (;; i++) {
                    if (robots.charAt(i) == separator) continue;

                    System.out.println();
                    for (;robots.charAt(i) != separator; i++) {
                        stringBuilder.append(robots.charAt(i));
                    }
                    siteMapXmlLinks.add(stringBuilder.toString());
                    stringBuilder.delete(0,stringBuilder.length());
                    break;
                }
                keyFound = false;
            }
        }
        return siteMapXmlLinks;
    }


    /**
     * Поиск ссылок на Sitemap в robots.txt на вход подаётся файл
     * @param robotsFile
     * @return List<String> searchSiteMap
     */
//    public static List<String> searchSiteMap(File robotsFile) {
//        String keyForSearch = "sitemap:";                   // ключ для поиска ссылки на saitmap
//        List<String> siteMapXmlLinks = new ArrayList<String>();   // результирующий список для вывода из метода найденных ссылок
//
//        try {
//            FileInputStream fileInputStream = new FileInputStream(WORK_FOLDER_PATH + "/" + robotsFile);
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
//
//            String tmpStr;
//
//            while ((tmpStr = bufferedReader.readLine()) != null) {
//
//                System.out.println("---  В ЦИКЛЕ   ---");
//
//                if (tmpStr.toLowerCase().contains(keyForSearch)) {
//
//                    System.out.println("tmpStr   ---   " + tmpStr);
//
//
//                    tmpStr.substring(keyForSearch.length() - 1, tmpStr.length() - 1);
//                }
//            }
//
//            fileInputStream.close();
//            bufferedReader.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return siteMapXmlLinks;
//    }


    /**
     * Поиск всех ссылок на страницы в переданном файле сайтмап
     * @param sitemap
     * @return
     */
    public static List<String> getLinkPagesFromSiteMap(String sitemap) {

        List<String> resultList = new ArrayList<String>();

        File sitemapFile = getFileByUrl(sitemap);

        // debug // Проверка правельности пути
//        System.out.println("str 127 --- " + sitemapFile.getPath());

        Integer mode = indexOrUrlset(sitemapFile.getPath());

        if (mode == 1) {

            // search site map

            List<String> sitemapList;

           // File sitemapXml = getFileByUrl(sitemap);

            sitemapList = searchLinkPagesInXml(sitemapFile.getPath());

            for (String s : sitemapList) {
//                System.out.println("str 143 --- " + s);

                resultList.addAll(getLinkPagesFromSiteMap(s));

            }

            sitemapFile.delete();

        } else if (mode == 2) {

            // search link pages in xml

            resultList.addAll(searchLinkPagesInXml(sitemapFile.getPath()));

            sitemapFile.delete();

        } else {

            // search link pages in txt

            resultList.addAll(getStringsByUrl(sitemap));

        }

        return resultList;
    }


    /**
     * Поиск страниц в Sitemap
     * @param xml
     * @return Список ссылок из входного sitemap
     */
    public static List<String> searchLinkPagesInXml(String xml) {

        System.out.println("Analyze :"+xml);
        if (!xml.contains("\\")) {
            xml = WORK_FOLDER_PATH + "/" + xml;
        }

        List<String> links = new LinkedList<>();
        boolean flag = false;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String compareDate = df.format(new Date()).toString().substring(0,10);

        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document document = documentBuilder.parse(xml);               // Скачиваем страницу

            Element root = document.getDocumentElement();                    // Получаем корневой элемент xml документа


            NodeList locList = root.getElementsByTagName("loc");
            // проверяем есть ли в файле lastmod
            NodeList lastmodList = root.getElementsByTagName("lastmod");



            if (lastmodList.getLength() == 0) flag = true; // если lastmod нет, то добавляем все ссылки

            System.out.println("Found "+locList.getLength()+ " nodes");
            for (int i = 0; i < locList.getLength(); i++) {
                //  System.out.println("Verify "+i+" node");
                Node locNode = locList.item(i);

                if (! flag) {
                    Node lastmodListNode = lastmodList.item(i);
                    if (compareDate.equals(lastmodListNode.getTextContent().substring(0,10))) {
                        System.out.println("Put node "+i+ "in list:"+locNode.getTextContent());
                        links.add(locNode.getTextContent());
                    }
                }
                else links.add(locNode.getTextContent());
            }


//            NodeList nodeList = root.getChildNodes();                     // Получаем список дочерних элиментов
//
//            for (int i = 0; i < nodeList.getLength(); i++) {              //
//                Node node = nodeList.item(i);                             //
//
//                NodeList nodeList1 = node.getChildNodes();                //
//                for (int j = 0; j < nodeList1.getLength(); j++) {
//
//                    if (nodeList1.item(j).getNodeName().equals("loc")) {  // ищем узлы "loc" и заносим значиения из этих узлов в результирующий список
//                        links.add(nodeList1.item(j).getTextContent());
////                        System.out.println();
//                    }
//                }
//            }
        } catch (ParserConfigurationException e) {
            LogWork.myPrintStackTrace(e);
        } catch (SAXException e) {
            LogWork.myPrintStackTrace(e);
        } catch (IOException e) {
            LogWork.myPrintStackTrace(e);
        }
        if(links.isEmpty())
            System.out.println("Ссылок не найдено!");
        else
            System.out.println(links.size()+" cсылок найдено!");
        return links;
    }


    /**
     * Поиск Saitmap'ов в sitemapindex
     * @param xml
     * @return Список ссылок из входного sitemap
     */
//    public static List<String> searchSiteMapInXmlString(String xml) {
//
//        if (!xml.contains("\\")) {
//            xml = WORK_FOLDER_PATH + "/" + xml;
//        }
//
//        List<String> links = new LinkedList<String>();
//        try {
//            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            Document document = documentBuilder.parse(xml);     // Скачиваем страницу
//            Node root = document.getDocumentElement();                    // Получаем корневой элемент xml документа
//            NodeList nodeList = root.getChildNodes();                     // Получаем список дочерних элиментов
//            for (int i = 0; i < nodeList.getLength(); i++) {              //
//                Node node = nodeList.item(i);                             //
//                NodeList nodeList1 = node.getChildNodes();                //
//                for (int j = 0; j < nodeList1.getLength(); j++) {
//                    if (nodeList1.item(j).getNodeName().equals("loc")) {  // ищем узлы "loc" и заносим значиения из этих узлов в результирующий список
//                        links.add(nodeList1.item(j).getTextContent());
////                        System.out.println();
//                    }
//                }
//            }
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if(links.isEmpty()) System.out.println("Ссылок не найдено!");
//        return links;
//    }


    /**
     * Поиск Saitmap'ов в sitemapindex
     * @param pathToXml
     * @return Список ссылок из входного sitemap
     */
//    private static List<String> searchSiteMapInXmlFile(String pathToXml) {
//        List<String> links = new LinkedList<String>();
//        try {
//            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            Document document = documentBuilder.parse(pathToXml);     // Скачиваем страницу
//            Node root = document.getDocumentElement();                    // Получаем корневой элемент xml документа
//            NodeList nodeList = root.getChildNodes();                     // Получаем список дочерних элиментов
//            for (int i = 0; i < nodeList.getLength(); i++) {              //
//                Node node = nodeList.item(i);                             //
//                NodeList nodeList1 = node.getChildNodes();                //
//                for (int j = 0; j < nodeList1.getLength(); j++) {
//                    if (nodeList1.item(j).getNodeName().equals("loc")) {  // ищем узлы "loc" и заносим значиения из этих узлов в результирующий список
//                        links.addAll(parseSiteMap(nodeList1.item(j).getTextContent()));
//                    }
//                }
//            }
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if(links.isEmpty()) System.out.println("Ссылок не найдено!");
//        return links;
//    }


    /**
     * Определение Saitmap или Sitemapindex
     * @param xml
     * @return Список ссылок из входного sitemap
     */
    private static int indexOrUrlset(String xml) {
        File file;
        if (!xml.contains("\\")) {
            file = new File(WORK_FOLDER_PATH + "/" + xml);
        }
        else {
            file = new File(xml);
        }

        if (isXml(file)) {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(file);               // Скачиваем страницу
                Node root = document.getDocumentElement();                    // Получаем корневой элемент xml документа


                if (root.getNodeName().toLowerCase().equals("sitemapindex")) {
                    return 1;
                } else if (root.getNodeName().toLowerCase().equals("urlset")) {
                    return 2;
                }
            } catch (ParserConfigurationException e) {
                LogWork.myPrintStackTrace(e);
            } catch (SAXException e) {
                LogWork.myPrintStackTrace(e);
            } catch (IOException e) {
                LogWork.myPrintStackTrace(e);
            }
        }
        return 0;
    }


//    /**
//     * Принемает ссылку на файл и обробатывет его в зависимости от фйормата (xml или нет)
//     * @param url
//     * @return списо ссылок на страницы
//     */
//    public static List<String> parseSiteMap(String url) {
//        if (isXml(url)) {              // если файл имеет формат xml
//            return searchSiteMapInXmlString(url);
//        } else {                          // парсим как обычный текст
//            return getStringsByUrl(url);
//        }
//    }


//    /**
//     * Поиск целевых строк в странице и подсчёт их количества
//     * @param page
//     * @param searchString
//     * @return колличество найденных вхождений переданной строки
//     */
//    public static int parsePage(String page, String searchString) {
//        int count = 0;
//        String preparedSearchString = searchString.trim();
//        for (String s : page.split("[\\s,.;:!?« »<=>\"–\\-]")) { // Между ковычками не пробел, это какойто другой не видимый символ
//            if(s.trim().equalsIgnoreCase(preparedSearchString)) count++;
//        }
//        return count;
//    }

//    public static int[] parsePageSuper(String page, ArrayList<String> keywords) {
//        int[] ranks = new int[keywords.size()];
//        int index;
//        int c = 0;
//
//        for (String s : page.split("[\\s,.;:!?« »<=>\"–\\-]")) { // Между ковычками не пробел, это какойто другой не видимый символ
//                c++;
//                index = keywords.indexOf(s.trim().toLowerCase());
//                if (index>=0)
//                    ranks[index]++;
//        }
//   //     System.out.println("Count iterations: "+c);
//        return ranks;
//    }

    public static int[] parsePageSuperPuper(String page, ArrayList<String> keywords) {
        int[] ranks = new int[keywords.size()];
        int index;
        int startIndex = page.length();
        int c = 0;

        page = page.toLowerCase();

        for (int i = 0; i<keywords.size(); i++) {

            index = page.indexOf(keywords.get(i));
            if (index>0 && index<startIndex) startIndex = index;
        }

        if (startIndex< page.length()) {

            page = page.substring(startIndex);

            while (page.contains("  "))
            page = page.replaceAll("\\s* \\s*"," ");

            for (String s : page.split("[\\s,.;:!?« »<=>\"–\\-]")) { // Между ковычками не пробел, это какойто другой не видимый символ
                c++;
                index = keywords.indexOf(s.trim());
                if (index >= 0)
                    ranks[index]++;
            }
        }
       // System.out.println("Count iterations: "+c);
        return ranks;
    }

    /**
     * Проверка строки на соответствие формату xml через прямое подключение
     * @param urlStr
     * @return true / false
     */
//    private static boolean isXml(String urlStr) {
//
//        String adr= urlStr;
//        StringBuilder stringBuilder = new StringBuilder();
//        URL url = null; //создаем URL
//        BufferedReader br;
//        String str = null;
//
//
//
//        try {
//            url = new URL(adr);
//            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); //открываем соединение
//            br = new BufferedReader(new InputStreamReader(conn.getInputStream())); // используем объект класса BufferedReader для работы со строками
//            str = br.readLine();
//
//            br.close(); //закрываем поток
//            conn.disconnect(); //закрываем соединение
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return str.contains("<?xml");
//    }


    /**
     * Проверка строки на соответствие формату xml через прямое подключение
     * @param file
     * @return true / false
     */
    private static boolean isXml(File file) {

        String str = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            str = bufferedReader.readLine().toLowerCase();
            fileInputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            LogWork.myPrintStackTrace(e);
        }
        return str.contains("<?xml");
    }


    /**
     * Скачивает, по переданной ссылке, построчно файл. Возвращает список строк.
     * @param urlStr
     * @return список строк
     */
    public static List<String> getStringsByUrl(String urlStr) {

        String adr= urlStr;
        BufferedReader br;
        String str;
        URL url = null; //создаем URL
        List<String> resultList = new ArrayList<String>();

        try {
            url = new URL(adr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection(); //открываем соединение
            br = new BufferedReader(new InputStreamReader(conn.getInputStream())); // используем объект класса BufferedReader для работы со строками

            while((str = br.readLine()) != null){ // пока не достигнут конец, считываем страницу построчно
                resultList.add(str);
            }
            br.close(); //закрываем поток
            conn.disconnect(); //закрываем соединение

        } catch (MalformedURLException e) {
            LogWork.myPrintStackTrace(e);
        } catch (IOException e) {
            LogWork.myPrintStackTrace(e);
        }
        return resultList;
    }


    /**
     * Скачивает, по переданной ссылке, построчно файл.
     * Создаёт файл со случайным именем по куазанному пути folderPath.
     * После использования файла в программе, его нужно удалить.
     * @param urlStr
     * @return случайное имя созданного файла
     */
    private static String downloadFileByUrl(String urlStr) {

        prepareStorage(WORK_FOLDER_PATH);
        String[] nameParts = urlStr.split("[.]");

        File workFolder = new File(WORK_FOLDER_PATH);
        File[] filesList = workFolder.listFiles();
        String randomName = "zzz";
        boolean coincidence = false;

        while (randomName.equals("zzz") ||  coincidence) {
            randomName = generateString();

            for (File f : filesList) {
                if (f.getName().equals(randomName)) {
                    coincidence = true;
                    break;
                } else {
                    coincidence = false;
                }
            }
        }

        randomName = randomName + "." + nameParts[nameParts.length - 1]; // добавляем к имени файла первоначальное расширение

        File resultFile = new File(WORK_FOLDER_PATH + "/" + randomName);

        URL url = null;

        HttpURLConnection conn;
        String tmpStr;

        if(resultFile.exists()) resultFile.delete();
        try {
            resultFile.createNewFile();
        } catch (IOException e) {
            LogWork.myPrintStackTrace(e);
        }

        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();

            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            FileOutputStream fileOutputStream = new FileOutputStream(resultFile);

            byte[] b = new byte[1024];
            int count = 0;

            while ((count=bis.read(b)) != -1)
                fileOutputStream.write(b,0,count);

            fileOutputStream.close();
        } catch (IOException e) {
            LogWork.myPrintStackTrace(e);
        }

        return randomName;
    }

    /**
     * Возвращает объект File, представляющий собой XML-файл
     * (содержащий в себе либо список нижележащих sitemap файлов, либо список ссылок, либо список архивов)
     * @param urlStr
     * @return
     */
    public static File getFileByUrl(String urlStr) {

        String fileName = downloadFileByUrl(urlStr);

        String[] fileNameArr = fileName.toLowerCase().split("[.]");

        if (fileNameArr[fileNameArr.length - 1].equals("gz")) {

            File result = ArchiveWorker.decompressGzipFile(fileName,WORK_FOLDER_PATH,true);

            File archive = new File(WORK_FOLDER_PATH + "/" + fileName);

            archive.delete();

            return result;
        } else {
            return new File(WORK_FOLDER_PATH + "/" + fileName);
        }
    }


    /**
     * Проверка и создание рабочего каталога для скаченных файлов
     * @param storageAddr
     */
    private static void prepareStorage (String storageAddr) {
        File storage = new File(storageAddr);
        if (!storage.exists()) storage.mkdir();
    }

    private static String generateString() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replace("-", "");
    }

    /**
     * Метод находит все ссылки на странице которые начинаются с сылки переданной методу
     * @param URL
     * @return список ссылок со страницы
     */
    public static List<String> collectLinkOnPage(String URL) {
        List<String> links;
        List<String> result = new ArrayList<>();
        org.jsoup.nodes.Document doc = null;

        try {
            doc  = Jsoup.connect(URL).get();
            // Логирование кода
            if (logLevel >= 1) LogWork.logWrite("doc = " + doc.title());
        } catch (IOException e) {
            LogWork.myPrintStackTrace(e);
        }
//        assert doc != null;
        if (doc != null) {
            links = doc.select("a").eachAttr("href");

            String startAddr = URL.replaceAll("\\.[a-z]*/.*$", "");

            for (String s : links) {
                if (s.startsWith(startAddr) || s.startsWith("/"))
                    result.add(s);
            }
        }
        return result;
    }
}
