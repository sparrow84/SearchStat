package dbService;

import dbService.dao.*;
import dbService.dataSets.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Date;
import java.util.List;

public class DBService {

    private final SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;

    public DBService(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }


    /**
     * добавление новой Персоны в БазуДанных
     * @param name - имя персоны
     * @return id добавленной персоны или -1 если операция не удалась
     */
    public int addPerson(String name){
        int id = -1;
        try {
            openSessionAndTransation();
            PersonDAO personDAO = new PersonDAO(session);
            Person person = personDAO.getPersonByName(name);
            if(!(person==null))
            {
                System.out.println("Такой Person уже есть в БД!");
                closeSessionAndTransation("commit");
                return id;
            }
            id = personDAO.insertPerson(name);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } catch (NullPointerException e) {
            System.err.println("something wrong");
        }
        finally {
            return id;
        }
    }

    public int writeRank(Person person, Page page, int rank) {
        int id = -1;
        try {
            openSessionAndTransation();
            PersonPageRankDAO personPageRankDAO = new PersonPageRankDAO(session);
            PersonPageRank personPageRank = personPageRankDAO.getPersonPageRank(person, page);
            if(!(personPageRank==null))
            {
                personPageRankDAO.updateRank(person,page, rank);
                closeSessionAndTransation("commit");
                return id;
            }
            id = personPageRankDAO.insertRank(person, page, rank);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } catch (NullPointerException e) {
            System.err.println("something wrong");
        }
        finally {
            return id;
        }
    }

    /**
     * получение Person из БД по id
     * @param id
     * @return найденный экземпляр Person
     */
    public Person getPersonById(int id) {
        Person person = null;
        try {
            openSessionAndTransation();
            PersonDAO personDAO = new PersonDAO(session);
            person = personDAO.getPersonById(id);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            if(person == null) System.out.println("Такого Person не найдено!");
            return person;
        }
    }



    /**
     * получение Person из БД по name
     * @param name
     * @return найденный экземпляр Person
     */
    public Person getPersonByName(String name) {
        Person person = null;
        try {
            openSessionAndTransation();
            PersonDAO personDAO = new PersonDAO(session);
            person = personDAO.getPersonByName(name);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("Попытка найти по имени Персону, которых в БД более одной(например два Vasya и его ищем => ошибка)");
        } finally {
            if(person == null) System.out.println("Такого Person не найдено!");
            return person;
        }
    }



    /**
     * добавление новой Page в БД
     * @param url
     * @param site
     * @param foundDateTime
     * @param lastScanDate
     * @return id добавленной Page или -1 если операция не удалась
     */
    public int addPage(String url, Site site, Date foundDateTime, Date lastScanDate, String type_page){
        int id = -1;
        try {
            openSessionAndTransation();
            PageDAO pageDAO = new PageDAO(session);
            id = pageDAO.insertPage(url, site, foundDateTime, lastScanDate, type_page);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return id;
        }
    }

    public int updatePageDate(Page page){
        int id = -1;
        try {
            openSessionAndTransation();
            PageDAO pageDAO = new PageDAO(session);
            pageDAO.updatePageDate(page);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return id;
        }
    }

    public int updatePageDateAndType(Page page, String type_page){
        int id = -1;
        try {
            openSessionAndTransation();
            PageDAO pageDAO = new PageDAO(session);
            pageDAO.updatePageDateAndType(page, type_page);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return id;
        }
    }


    public Page getPageById(int id) {
        Page page = null;
        try {
            openSessionAndTransation();
            PageDAO pageDAO = new PageDAO(session);
            page = pageDAO.getPageById(id);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            if(page == null) System.out.println("Такой Page не найдено!");
            return page;
        }
    }


    /**
     * добавление нового Site в БД
     * @param name
     * @return id добавленного Site или -1 если операция не удалась
     */
    public int addSite(String name) {
        int id = -1;
        try {
            openSessionAndTransation();
            SiteDAO siteDAO = new SiteDAO(session);
            id = siteDAO.insertSite(name);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return id;
        }
    }

    public Site getSiteByName(String name) {
        Site site = null;
        try {
            openSessionAndTransation();
            SiteDAO siteDAO = new SiteDAO(session);
            site = siteDAO.getSiteByName(name);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            e.printStackTrace();
        } finally {
            if(site == null) System.out.println("Такого Site не найдено!");
            return site;
        }
    }

    public List<Site> getAllSite() {
        List<Site> sites = null;
        try {
            openSessionAndTransation();
            SiteDAO siteDAO = new SiteDAO(session);
            sites = siteDAO.getAllSite();
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return sites;
        }
    }

    public List<Site> gettAllSiteWithoutPage() {
        List<Site> sites = null;
        try {
            openSessionAndTransation();
            SiteDAO siteDAO = new SiteDAO(session);
            sites = siteDAO.getAllSiteWithoutPage();
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return sites;
        }
    }

    public List<Page> getNonScannedPages() {
        List<Page> pages = null;
        try {
            openSessionAndTransation();
            PageDAO pageDAO = new PageDAO(session);
            pages = pageDAO.getNonScannedPages();
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return pages;
        }
    }


    public void resetOldPages(String type) {

        try {
            openSessionAndTransation();
            PageDAO pageDAO = new PageDAO(session);
            pageDAO.resetOldSiteMap(new Date(), type);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        }

    }

    public List<Keyword> getKeywordByPerson(Person person) {
        List<Keyword> keywords = null;
        try {
            openSessionAndTransation();
            KeywordDAO keyywordDAO = new KeywordDAO(session);
            keywords = keyywordDAO.getKeywordByPerson(person);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return keywords;
        }
    }

    public List<Keyword> getAllKeywords() {
        List<Keyword> keywords = null;
        try {
            openSessionAndTransation();
            KeywordDAO keyywordDAO = new KeywordDAO(session);
            keywords = keyywordDAO.getAllKeywords();
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return keywords;
        }
    }

    public List<Person> getAllPerson() {
        List<Person> persons = null;
        try{
            openSessionAndTransation();
            PersonDAO personDAO = new PersonDAO(session);
            persons = personDAO.getAllPerson();
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return persons;
        }
    }


    public int addKeyword(Person person, String name) {
        int id = -1;
        try {
            openSessionAndTransation();
            KeywordDAO keywordDAO = new KeywordDAO(session);
            id = keywordDAO.insertKeyword(person, name);
            closeSessionAndTransation("commit");
        } catch (HibernateException e) {
            closeSessionAndTransation("rollback");
            System.err.println("!!!HIBERNATE ERROR APPEARED!!!");
        } finally {
            return id;
        }
    }

    private void openSessionAndTransation() {
        this.session = this.sessionFactory.openSession();
        this.transaction = session.beginTransaction();
    }

    private void closeSessionAndTransation(String mode) {
        if (mode.equals("commit")) {
            this.transaction.commit();
        } else {
            this.transaction.rollback();
        }
        this.session.close();
        this.transaction = null;
        this.session = null;
    }



}
