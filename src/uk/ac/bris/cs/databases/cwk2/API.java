package uk.ac.bris.cs.databases.cwk2;

import java.sql.*;
import java.util.List;
import java.util.*;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.AdvancedForumSummaryView;
import uk.ac.bris.cs.databases.api.AdvancedForumView;
import uk.ac.bris.cs.databases.api.ForumSummaryView;
import uk.ac.bris.cs.databases.api.ForumView;
import uk.ac.bris.cs.databases.api.AdvancedPersonView;
import uk.ac.bris.cs.databases.api.PostView;
import uk.ac.bris.cs.databases.api.Result;
import uk.ac.bris.cs.databases.api.PersonView;
import uk.ac.bris.cs.databases.api.SimpleForumSummaryView;
import uk.ac.bris.cs.databases.api.SimpleTopicView;
import uk.ac.bris.cs.databases.api.TopicView;
import uk.ac.bris.cs.databases.api.SimpleTopicSummaryView;
import uk.ac.bris.cs.databases.api.SimplePostView;
import uk.ac.bris.cs.databases.api.TopicSummaryView;
import java.util.Date;
import java.text.SimpleDateFormat;
/**
*
* @author csxdb
*/
public class API implements APIProvider {

    private final Connection c;

    public API(Connection c) {
        this.c = c;
    }

    /* A.1 */

    @Override
    public Result<Map<String, String>> getUsers() {
        ResultSet r;
        Map<String, String> user = new HashMap<String, String>();
        String q = "SELECT * FROM Person;";
        try (PreparedStatement s = this.c.prepareStatement(q)) {
            r = s.executeQuery();
            while(r.next()){
                user.put(r.getString("username"), r.getString("name"));
            }
            c.commit();
            return Result.success(user);
        } catch (SQLException e) {
            try {
                this.c.rollback();
                return Result.fatal("Can not fetch the users !");
            } catch (SQLException f) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<PersonView> getPersonView(String username) {
        if (username == null || username.isEmpty()) {
            return Result.failure("getPersonView: username cannot be empty");
        }
        ResultSet r;
        String q = "SELECT * FROM Person WHERE username = ?;";
        try (PreparedStatement s = this.c.prepareStatement(q)) {
            s.setString(1, username);
            r = s.executeQuery();
            if(r.next()) {
                String name = r.getString("name");
                String userName = r.getString("username");
                String stuId = r.getString("stuId");
                if(stuId == null) {
                    stuId = "Not a student !";
                }
                c.commit();
                return Result.success(new PersonView(name, userName, stuId));
            }
            else {
                c.commit();
                return Result.failure("No Person with given username !");
            }
        } catch (SQLException e) {
            try {
                this.c.rollback();
                return Result.fatal("Can not create person view !");
            } catch (SQLException f) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result addNewPerson(String name, String username, String studentId) {
        if(name == null || name.isEmpty()) {
            return Result.failure("addNewPerson: name cannot be empty");
        }
        if(username == null || username.isEmpty()) {
            return Result.failure("addNewPerson: username cannot be empty");
        }
        if(!(studentId == null)) {
            if(studentId.isEmpty()) {
                return Result.failure("addNewPerson: studentId cannot be empty");
            }
        }
        String qSearch = "SELECT * FROM Person WHERE username = ? OR stuId = ?;";
        ResultSet r;
        try (PreparedStatement sSearch = this.c.prepareStatement(qSearch)) {
            sSearch.setString(1, username);
            sSearch.setString(2, studentId);
            r = sSearch.executeQuery();
            if(!r.next()) {
                String qInsert = "INSERT INTO Person (name, username, stuId) VALUES(?, ?, ?);";
                try (PreparedStatement sInsert = this.c.prepareStatement(qInsert)) {
                    sInsert.setString(1, name);
                    sInsert.setString(2, username);
                    sInsert.setString(3, studentId);
                    sInsert.executeQuery();
                    c.commit();
                    return Result.success("Insertion succeeded !");
                } catch (SQLException eOne) {
                    try {
                        this.c.rollback();
                        return Result.fatal("Can not create user !");
                    } catch (SQLException fOne) {
                        return Result.fatal("Rolling back failed !");
                    }
                }
            }
            else {
                try {
                    c.commit();
                    return Result.failure("Username or studentId provided exits !");
                } catch (SQLException fTwo) {
                    try {
                        this.c.rollback();
                        return Result.fatal("Commit failed !");
                    } catch (SQLException gOne) {
                        return Result.fatal("Rolling back failed !");
                    }
                }
            }
        } catch(SQLException eTwo) {
            try {
                this.c.rollback();
                return Result.fatal("System failed !");
            } catch (SQLException fThree) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /* A.2 */

    @Override
    public Result<List<SimpleForumSummaryView>> getSimpleForums() {
        String q = "SELECT * FROM SimpleForum ORDER BY title ASC;";
        ArrayList<SimpleForumSummaryView> forums = new ArrayList<>();
        ResultSet r;
        try (PreparedStatement s = this.c.prepareStatement(q)) {
            r = s.executeQuery();
            while(r.next()) {
                forums.add(new SimpleForumSummaryView(r.getInt("id"), r.getString("title")));
            }
            c.commit();
            return Result.success(forums);
        } catch (SQLException e) {
            try {
                c.rollback();
                return Result.fatal("Can not get Simple Forum !");
            } catch (SQLException f) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result createForum(String title) {
        String qSearch = "SELECT * FROM SimpleForum WHERE title = ?;";
        ResultSet r;
        if(title == null || title.isEmpty()) {
            return Result.failure("Forum title invalid !");
        }
        else {
            try (PreparedStatement sSearch = this.c.prepareStatement(qSearch)) {
                sSearch.setString(1, title);
                r = sSearch.executeQuery();
                if(!r.next()) {
                    String qInsert = "INSERT INTO SimpleForum (title) VALUES (?);";
                    try (PreparedStatement sInsert = this.c.prepareStatement(qInsert)) {
                        sInsert.setString(1, title);
                        sInsert.executeQuery();
                        c.commit();
                        return Result.success("Inserting forum succeeded !");
                    } catch (SQLException eOne) {
                        try {
                            c.rollback();
                            return Result.fatal("System failure !");
                        } catch (SQLException eTwo) {
                            return Result.fatal("Rolling back failed !");
                        }
                    }
                }
                else {
                    c.commit();
                    return Result.failure("Title duplication !");
                }
            } catch (SQLException eThree) {
                try {
                    c.rollback();
                    return Result.fatal("Can not insert new forum !");
                } catch (SQLException eFour) {
                    return Result.fatal("Rolling back failed !");
                }
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /* A.3 */

    @Override
    public Result<List<ForumSummaryView>> getForums() {

        String q = "SELECT * FROM SimpleForum ORDER BY title ASC;";
        ArrayList<ForumSummaryView> forums = new ArrayList<>();
        ResultSet r;
        try (PreparedStatement s = this.c.prepareStatement(q)) {
            r = s.executeQuery();
            while(r.next()) {
                ResultSet r2;
                SimpleTopicSummaryView topic;
                int temp = r.getInt("id");
                String q2 = "SELECT * FROM SimpleForum s INNER JOIN Topic t ON s.id = t.forum " +
                "WHERE s.id = ? ORDER BY t.id DESC LIMIT 1;";
                try (PreparedStatement h = this.c.prepareStatement(q2)) {
                    h.setInt(1, temp);
                    r2 = h.executeQuery();
                    if(r2.next()) {
                        topic = new SimpleTopicSummaryView(r2.getInt("id"), r2.getInt("forum"), r2.getString("name"));
                    }
                    else {
                        topic = null;
                    }
                    c.commit();
                } catch (SQLException i) {
                    try {
                        c.rollback();
                        return Result.fatal("Can not get Forums * !");
                    } catch (SQLException a) {
                        return Result.fatal("Rolling back failed !");
                    }
                }
                forums.add(new ForumSummaryView(r.getInt("id"), r.getString("title"), topic));
            }
            c.commit();
            return Result.success(forums);
        } catch (SQLException e) {
            try {
                c.rollback();
                return Result.fatal("Can not get Forums !");
            } catch (SQLException f) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<ForumView> getForum(int id) {

        ResultSet r1;
        ResultSet r2;
        ArrayList<SimpleTopicSummaryView> topics = new ArrayList<SimpleTopicSummaryView>();
        String q1 = "SELECT * FROM Topic WHERE forum = ? ORDER BY id ASC";
        String q2 = "SELECT * FROM SimpleForum WHERE id = ?";
        try (PreparedStatement s2 = this.c.prepareStatement(q2)) {
            s2.setInt(1, id);
            r2 = s2.executeQuery();
            if(!r2.next()) {
                return Result.failure("No forum with given id");
            }
            try (PreparedStatement s1 = this.c.prepareStatement(q1)) {
                s1.setInt(1, r2.getInt("id"));
                r1 = s1.executeQuery();
                while(r1.next()){
                    topics.add(new SimpleTopicSummaryView(r1.getInt("id"), r1.getInt("forum"), r1.getString("name")));
                }
            } catch (SQLException e) {
                try {
                    this.c.rollback();
                    return Result.fatal("Can not fetch the forums1 !");
                } catch (SQLException f) {
                    return Result.fatal("Rolling back failed !");
                }
            }
            c.commit();
            return Result.success(new ForumView(r2.getInt("id"), r2.getString("title"), topics));

        } catch (SQLException e) {
            try {
                this.c.rollback();
                return Result.fatal("Can not fetch the forums2 !");
            } catch (SQLException f) {
                return Result.fatal("Rolling back failed !");
            }
        }
    }

    @Override
    public Result<SimpleTopicView> getSimpleTopic(int topicId) {
        String qTopic = "SELECT * FROM Topic WHERE id = ?;";
        ResultSet rOne;
        ResultSet rTwo;
        ResultSet rThree;
        int postNumber;
        String title;
        ArrayList<SimplePostView> post = new ArrayList<>();
        try (PreparedStatement sTopic = this.c.prepareStatement(qTopic)) {
            sTopic.setInt(1, topicId);
            rOne = sTopic.executeQuery();
            if(!rOne.next()) {
                return Result.failure("No topic with given topic Id");
            }
            title = rOne.getString("name");
            String qPostNum = "SELECT COUNT(*) AS num FROM Post WHERE topic = ?;";
            try (PreparedStatement sPostNum = this.c.prepareStatement(qPostNum)) {
                sPostNum.setInt(1, topicId);
                rThree = sPostNum.executeQuery();
                rThree.next();
                postNumber = rThree.getInt("num");
            } catch (SQLException eThree) {
                try {
                    c.rollback();
                    return Result.fatal("Can not getSimpleTopic * !");
                } catch (SQLException eFour) {
                    return Result.fatal("Rolling back failed !");
                }
            }
            String qPost = "SELECT * FROM Post po INNER JOIN Person pe ON po.author = pe.id WHERE topic = ?;";
            try (PreparedStatement sPost = this.c.prepareStatement(qPost)) {
                sPost.setInt(1, topicId);
                rTwo = sPost.executeQuery();
                while(rTwo.next()) {
                    post.add(new SimplePostView(postNumber, rTwo.getString("name"), rTwo.getString("content"), rTwo.getString("atTime")));
                }
            } catch (SQLException eThree) {
                try {
                    c.rollback();
                    return Result.fatal("Can not getSimpleTopic ** !");
                } catch (SQLException eFour) {
                    return Result.fatal("Rolling back failed !");
                }
            }
            c.commit();
            return Result.success(new SimpleTopicView(topicId, rOne.getString("name"), post));
        } catch (SQLException eOne) {
            try {
                c.rollback();
                return Result.fatal("Can not getSimpleTopic *** !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    //need verify !!!
    @Override
    public Result<PostView> getLatestPost(int topicId) {
        String qTopic = "SELECT * FROM Topic WHERE id = ?;";
        ResultSet rTopic;
        try (PreparedStatement sTopic = this.c.prepareStatement(qTopic)) {
            sTopic.setInt(1, topicId);
            rTopic = sTopic.executeQuery();
            if(!rTopic.next()) {
                return Result.failure("No topic with given topic Id");
            }
        } catch (SQLException ex) {
            try {
                c.rollback();
                return Result.fatal("Can not getLatestPost *** !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        int postNumber;
        ResultSet rThree;
        String qPostNum = "SELECT COUNT(*) AS num FROM Post WHERE topic = ?;";
        try (PreparedStatement sPostNum = this.c.prepareStatement(qPostNum)) {
            sPostNum.setInt(1, topicId);
            rThree = sPostNum.executeQuery();
            rThree.next();
            postNumber = rThree.getInt("num");
        } catch (SQLException eThree) {
            try {
                c.rollback();
                return Result.fatal("Can not getSimpleTopic * !");
            } catch (SQLException eFour) {
                return Result.fatal("Rolling back failed !");
            }
        }

        ResultSet rTwo;
        String qForum = "SELECT DISTINCT s.id as forumId FROM SimpleForum s " +
        "INNER JOIN Topic t ON s.id = t.forum " +
        "WHERE t.id = ?;";
        try(PreparedStatement sForum = this.c.prepareStatement(qForum)) {
            sForum.setInt(1, topicId);
            rTwo = sForum.executeQuery();
            rTwo.next();
        } catch (SQLException eThree) {
            try {
                c.rollback();
                return Result.fatal("Can not getLatestPost !");
            } catch (SQLException eFour) {
                return Result.fatal("Rolling back failed !");
            }
        }
        ResultSet rOne;
        String qPost = "SELECT * FROM Post po INNER JOIN Person pe ON po.author = pe.id WHERE topic = ? ORDER BY po.id DESC LIMIT 1;";
        try (PreparedStatement sPost = this.c.prepareStatement(qPost)) {
            sPost.setInt(1, topicId);
            rOne = sPost.executeQuery();
            rOne.next();
            c.commit();
            return Result.success(new PostView(rTwo.getInt("forumId"), topicId, postNumber, rOne.getString("name"),
            rOne.getString("username"), rOne.getString("content"), rOne.getString("atTime"), 18));
        } catch (SQLException eOne) {
            try {
                c.rollback();
                return Result.fatal("Can not getLatestPost !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result createPost(int topicId, String username, String text) {
        String qTopic = "SELECT * FROM Topic WHERE id = ?;";
        ResultSet rTopic;
        try (PreparedStatement sTopic = this.c.prepareStatement(qTopic)) {
            sTopic.setInt(1, topicId);
            rTopic = sTopic.executeQuery();
            if(!rTopic.next()) {
                return Result.failure("No topic with given topic Id");
            }
        } catch (SQLException ex) {
            try {
                c.rollback();
                return Result.fatal("Can not getLatestPost *** !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        ResultSet resultPersonId;
        int personId;
        String qPerson = "SELECT * FROM Person WHERE username = ?";
        if(username == null || username.isEmpty()) {
            return Result.failure("Invalid username !");
        }
        if(text == null || text.isEmpty()) {
            return Result.failure("Invalid text content !");
        }
        try (PreparedStatement sPerson = this.c.prepareStatement(qPerson)) {
            sPerson.setString(1, username);
            resultPersonId = sPerson.executeQuery();
            resultPersonId.next();
            personId = resultPersonId.getInt("id");
            Date d = new Date();
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
            String qInsert = "INSERT INTO Post (topic, author, content, atTime) " +
            "VALUES (?, ?, ?, ?);";
            try (PreparedStatement sInsert = this.c.prepareStatement(qInsert)) {
                sInsert.setInt(1, topicId);
                sInsert.setInt(2, personId);
                sInsert.setString(3, text);
                sInsert.setString(4, time.format(d));
                sInsert.executeQuery();
            } catch (SQLException eThree) {
                try {
                    c.rollback();
                    return Result.fatal("Can not create post, database rooling back 001 !");
                } catch (SQLException eFour) {
                    return Result.fatal("Rolling back failed !");
                }
            }
            c.commit();
            return Result.success("Posted !");
        } catch (SQLException eOne) {
            try {
                c.rollback();
                return Result.fatal("Can not create post, database rooling back 002 !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result createTopic(int forumId, String username, String title, String text) {
        if(username == null || username.isEmpty()) {
            return Result.failure("Invalid username !");
        }
        String qForum = "SELECT * FROM SimpleForum WHERE id = ?;";
        ResultSet rForum;
        try (PreparedStatement sForum = this.c.prepareStatement(qForum)) {
            sForum.setInt(1, forumId);
            rForum = sForum.executeQuery();
            if(!rForum.next()) {
                return Result.failure("No forum with given forum Id");
            }
        } catch (SQLException ex) {
            try {
                c.rollback();
                return Result.fatal("Can not createTopic *** !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        if(title == null || title.isEmpty()) {
            return Result.failure("title cannot be null or empty");
        }
        if(text == null || text .isEmpty()) {
            return Result.failure("text cannot be null or empty");
        }
        ResultSet resultPersonId;
        int personId;
        int topicId;
        String qPerson = "SELECT * FROM Person WHERE username = ?";
        try (PreparedStatement sPerson = this.c.prepareStatement(qPerson)) {
            sPerson.setString(1, username);
            resultPersonId = sPerson.executeQuery();
            if(!resultPersonId.next()) {
                return Result.failure("No person with the given username");
            }
            personId = resultPersonId.getInt("id");
            String qInsertOne = "INSERT INTO Topic (name, forum) " +
            "VALUES (?, ?);";
            try (PreparedStatement sInsertOne = this.c.prepareStatement(qInsertOne)) {
                sInsertOne.setString(1, title);
                sInsertOne.setInt(2, forumId);
                sInsertOne.executeQuery();
                String qTopic = "SELECT * FROM Topic WHERE name = ?;";
                try (PreparedStatement sTopic = this.c.prepareStatement(qTopic)) {
                    sTopic.setString(1, title);
                    ResultSet temp = sTopic.executeQuery();
                    temp.next();
                    topicId = temp.getInt("id");
                    String qInsertTwo = "INSERT INTO Post (topic, author, content, atTime) " +
                    "VALUES (?, ?, ?, ?);";
                    try(PreparedStatement sInsertTwo = this.c.prepareStatement(qInsertTwo)) {
                        Date d = new Date();
                        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
                        sInsertTwo.setInt(1, topicId);
                        sInsertTwo.setInt(2, personId);
                        sInsertTwo.setString(3, text);
                        sInsertTwo.setString(4, time.format(d));
                        sInsertTwo.executeQuery();
                    } catch (SQLException eSeven) {
                        try {
                            c.rollback();
                            return Result.fatal("Can not create topic, database rooling back 004!");
                        } catch (SQLException eEight) {
                            return Result.fatal("Rolling back failed !");
                        }
                    }
                } catch (SQLException eFive) {
                    try {
                        c.rollback();
                        return Result.fatal("Can not create topic, database rooling back 001!");
                    } catch (SQLException eSix) {
                        return Result.fatal("Rolling back failed !");
                    }
                }
            } catch (SQLException eThree) {
                try {
                    c.rollback();
                    return Result.fatal("Can not create topic, database rooling back 002 !");
                } catch (SQLException eFour) {
                    return Result.fatal("Rolling back failed !");
                }
            }
            c.commit();
            return Result.success("!");
        } catch (SQLException eOne) {
            try {
                c.rollback();
                return Result.fatal("Can not create topic, database rooling back 003 !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<Integer> countPostsInTopic(int topicId) {
        String qTopic = "SELECT * FROM Topic WHERE id = ?;";
        ResultSet rTopic;
        try (PreparedStatement sTopic = this.c.prepareStatement(qTopic)) {
            sTopic.setInt(1, topicId);
            rTopic = sTopic.executeQuery();
            if(!rTopic.next()) {
                return Result.failure("No topic with given topic Id");
            }
        } catch (SQLException ex) {
            try {
                c.rollback();
                return Result.fatal("Can not getLatestPost *** !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        int numOfPost;
        ResultSet r;
        String qPostNum = "SELECT COUNT(*) AS num FROM Post WHERE topic = ?;";
        try (PreparedStatement sPostNum = this.c.prepareStatement(qPostNum)) {
            sPostNum.setInt(1, topicId);
            r = sPostNum.executeQuery();
            if(r.next()) {
                numOfPost = r.getInt("num");
            } else {
                numOfPost = 0;
            }
            c.commit();
            return Result.success(new Integer(numOfPost));
        } catch (SQLException eOne) {
            try {
                c.rollback();
                return Result.fatal("Can not count post, database rooling back 003 !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /* B.1 */

    @Override
    public Result likeTopic(String username, int topicId, boolean like) {
        String qTopic = "SELECT * FROM Topic WHERE id = ?;";
        ResultSet rTopic;
        try (PreparedStatement sTopic = this.c.prepareStatement(qTopic)) {
            sTopic.setInt(1, topicId);
            rTopic = sTopic.executeQuery();
            if(!rTopic.next()) {
                return Result.failure("No topic with given topic Id");
            }
        } catch (SQLException ex) {
            try {
                c.rollback();
                return Result.fatal("Can not getLatestPost *** !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        int personId;
        String qPerson = "SELECT * FROM Person WHERE username = ?;";
        try (PreparedStatement sPerson = this.c.prepareStatement(qPerson)) {
            ResultSet rPerson;
            sPerson.setString(1, username);
            rPerson = sPerson.executeQuery();
            if(!rPerson.next()){
                return Result.failure("User does not exists !");
            }
            personId = rPerson.getInt("id");
            String qCheckExistence = "SELECT COUNT(*) AS num FROM Favor " +
            "WHERE person = ? AND topic = ?;";
            try (PreparedStatement sCheckExistence =
            this.c.prepareStatement(qCheckExistence)) {
                int cnt;
                sCheckExistence.setInt(1, personId);
                sCheckExistence.setInt(2, topicId);
                ResultSet rExistence = sCheckExistence.executeQuery();
                rExistence.next();
                cnt = rExistence.getInt("num");
                if(cnt == 0 && like == true) {
                    String qInsert = "INSERT INTO Favor (person, topic) " +
                    "VALUES (?, ?);";
                    try (PreparedStatement sInsert = this.c.prepareStatement(qInsert)) {
                        sInsert.setInt(1, personId);
                        sInsert.setInt(2, topicId);
                        sInsert.executeQuery();
                    } catch (SQLException eThree) {
                        try {
                            c.rollback();
                            return Result.fatal("Can not like topic, database rolled back One !");
                        } catch (SQLException eFour) {
                            return Result.fatal("Rolling back failed !");
                        }
                    }
                }
                else if (cnt == 1 && like == true) {
                    c.commit();
                    return Result.success("Duplicate operation !");
                }
                else if (cnt == 1 && like == false) {
                    String qDelete = "DELETE FROM Favor WHERE person = ? AND topic = ?;";
                    try (PreparedStatement sDelete = this.c.prepareStatement(qDelete)) {
                        sDelete.setInt(1, personId);
                        sDelete.setInt(2, topicId);
                        sDelete.executeQuery();
                    } catch (SQLException eNine) {
                        try {
                            c.rollback();
                            return Result.fatal("Can not unlike topic, database rolled back Two !");
                        } catch (SQLException eTen) {
                            return Result.fatal("Rolling back failed !");
                        }
                    }
                }
                else {
                    c.commit();
                    return Result.success("No like present, no need to unlike !");
                }
            } catch (SQLException eFive) {
                try {
                    c.rollback();
                    return Result.fatal("Can not like topic, database rolled back Three!");
                } catch (SQLException eSix) {
                    return Result.fatal("Rolling back failed !");
                }
            }
            c.commit();
            return Result.success(" ");
        } catch (SQLException eOne) {
            try {
                c.rollback();
                return Result.fatal("Can not like topic, database rolled back Four!");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result likePost(String username, int topicId, int post, boolean like) {
        if(username == null || username.isEmpty()) {
            return Result.failure("username cannot be null or empty !");
        }
        if(!(like == true || like == false)) {
            return Result.failure("like cannot be true or false !");
        }
        String qPost = "SELECT * FROM Post WHERE id = ?;";
        ResultSet rPost;
        try (PreparedStatement sPost = this.c.prepareStatement(qPost)) {
            sPost.setInt(1, post);
            rPost = sPost.executeQuery();
            if(!rPost.next()) {
                return Result.failure("No post with given post Id");
            }
        } catch (SQLException ex) {
            try {
                c.rollback();
                return Result.fatal("Can not getLatestPost *** !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }

        String qTopic = "SELECT * FROM Topic WHERE id = ?;";
        try (PreparedStatement sTopic = this.c.prepareStatement(qTopic)) {
            sTopic.setInt(1, topicId);
            ResultSet rTopic = sTopic.executeQuery();
            if(!rTopic.next()){
                return Result.failure("Topic does not exist !");
            }
            int personId;
            String qPerson = "SELECT * FROM Person WHERE username = ?;";
            try (PreparedStatement sPerson = this.c.prepareStatement(qPerson)) {
                ResultSet rPerson;
                sPerson.setString(1, username);
                rPerson = sPerson.executeQuery();
                if(!rPerson.next()){
                    return Result.failure("User does not exists !");
                }
                personId = rPerson.getInt("id");
                String qCheckExistence = "SELECT COUNT(*) AS num FROM Love " +
                "WHERE person = ? AND post = ?;";
                try (PreparedStatement sCheckExistence =
                this.c.prepareStatement(qCheckExistence)) {
                    int cnt;
                    sCheckExistence.setInt(1, personId);
                    sCheckExistence.setInt(2, post);
                    ResultSet rExistence = sCheckExistence.executeQuery();
                    rExistence.next();
                    cnt = rExistence.getInt("num");
                    if(cnt == 0 && like == true) {
                        String qInsert = "INSERT INTO Love (person, post) " +
                        "VALUES (?, ?);";
                        try (PreparedStatement sInsert = this.c.prepareStatement(qInsert)) {
                            sInsert.setInt(1, personId);
                            sInsert.setInt(2, post);
                            sInsert.executeQuery();
                        } catch (SQLException eThree) {
                            try {
                                c.rollback();
                                return Result.fatal("Can not like post, database rolled back One !");
                            } catch (SQLException eFour) {
                                return Result.fatal("Rolling back failed !");
                            }
                        }
                    }
                    else if (cnt == 1 && like == true) {
                        c.commit();
                        return Result.success("Duplicate operation !");
                    }
                    else if (cnt == 1 && like == false) {
                        String qDelete = "DELETE FROM Love WHERE person = ? AND post = ?;";
                        try (PreparedStatement sDelete = this.c.prepareStatement(qDelete)) {
                            sDelete.setInt(1, personId);
                            sDelete.setInt(2, post);
                            sDelete.executeQuery();
                        } catch (SQLException eNine) {
                            try {
                                c.rollback();
                                return Result.fatal("Can not unlike post, database rolled back Two !");
                            } catch (SQLException eTen) {
                                return Result.fatal("Rolling back failed !");
                            }
                        }
                    }
                    else {
                        c.commit();
                        return Result.success("No like present, no need to unlike !");
                    }
                } catch (SQLException eFive) {
                    try {
                        c.rollback();
                        return Result.fatal("Can not like post, database rolled back Three!");
                    } catch (SQLException eSix) {
                        return Result.fatal("Rolling back failed !");
                    }
                }
                c.commit();
                return Result.success(" ");
            } catch (SQLException eOne) {
                try {
                    c.rollback();
                    return Result.fatal("Can not like post, database rolled back Four!");
                } catch (SQLException eTwo) {
                    return Result.fatal("Rolling back failed !");
                }
            }
        } catch (SQLException eOne) {
            try {
                c.rollback();
                return Result.fatal("Can not like post, database rolled back !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<List<PersonView>> getLikers(int topicId) {
        String qTopic = "SELECT * FROM Topic as t WHERE t.id = ?;";
        try (PreparedStatement sTopic = this.c.prepareStatement(qTopic)) {
            sTopic.setInt(1, topicId);
            ResultSet rTopic = sTopic.executeQuery();
            if(!rTopic.next()) {
                return Result.failure("No such topic !");
            }
            else {
                String qLiker = "SELECT p.name as personName, p.username as username, p.stuId as studentId " +
                "FROM Person as p INNER JOIN " +
                "Favor as f ON f.person = p.id INNER JOIN " +
                "Topic as t ON f.topic = t.id WHERE " +
                "t.id = ? ORDER BY p.name ASC;";
                ArrayList<PersonView> likers = new ArrayList<>();
                try(PreparedStatement sLiker = this.c.prepareStatement(qLiker)) {
                    sLiker.setInt(1, topicId);
                    ResultSet rLiker = sLiker.executeQuery();
                    while(rLiker.next()) {
                        PersonView temp = new PersonView(rLiker.getString("personName"),
                        rLiker.getString("username"), rLiker.getString("studentId"));
                        likers.add(temp);
                    }
                    c.commit();
                    return Result.success(likers);
                } catch(SQLException eOne) {
                    try {
                        c.rollback();
                        return Result.fatal("Can not get likers, database rolled back !");
                    } catch (SQLException eTwo) {
                        return Result.fatal("Rolling back failed !");
                    }
                }
            }
        } catch (SQLException eThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get likers, database rolled back !");
            } catch (SQLException eFour) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<TopicView> getTopic(int topicId) {
        int forumId;
        String forumName;
        String topicTitle;
        int postNumber;
        ArrayList<PostView> posts = new ArrayList<>();
        String qTopic = "SELECT * FROM Topic WHERE id = ?;";
        try (PreparedStatement sTopic = c.prepareStatement(qTopic)) {
            sTopic.setInt(1, topicId);
            ResultSet rTopic = sTopic.executeQuery();
            if(!rTopic.next()) {
                return Result.failure("No such topic !");
            }
            else {
                topicTitle = rTopic.getString("name");
                String qForum = "SELECT f.id as forumId, f.title as forumName FROM Topic as t INNER JOIN " +
                "SimpleForum as f ON t.forum = f.id WHERE " +
                "t.id = ?;";
                try (PreparedStatement sForum = this.c.prepareStatement(qForum)) {
                    sForum.setInt(1, topicId);
                    ResultSet rForum = sForum.executeQuery();
                    rForum.next();
                    forumId = rForum.getInt("forumId");
                    forumName = rForum.getString("forumName");
                    String qPost = "SELECT COUNT(*) AS postNumber FROM Post " +
                    "WHERE topic = ?;";
                    try (PreparedStatement sPost = c.prepareStatement(qPost)) {
                        sPost.setInt(1, topicId);
                        ResultSet rPost = sPost.executeQuery();
                        rPost.next();
                        postNumber = rPost.getInt("postNumber");
                        String qPostList = "SELECT pe.name as personName, pe.username as username, " +
                        "p.id as postId, p.content as text, p.atTime as postedAt, COUNT(*) as likes " +
                        "FROM Post as p INNER JOIN " +
                        "Person as pe ON p.author = pe.id WHERE p.Topic = ?;";
                        try (PreparedStatement sPostList = c.prepareStatement(qPostList)) {
                            sPostList.setInt(1, topicId);
                            ResultSet rPostList = sPostList.executeQuery();
                            while(rPostList.next()) {
                                String personName = rPostList.getString("personName");
                                String username = rPostList.getString("username");
                                String text = rPostList.getString("text");
                                String postedAt = rPostList.getString("postedAt");
                                int numberOfLikes = 0;
                                String qLikes = "SELECT COUNT(*) as numberOfLikes FROM Post as p INNER JOIN " +
                                "Love as l ON l.post = p.id WHERE p.id = ?;";
                                try (PreparedStatement sLikes = c.prepareStatement(qLikes)) {
                                    sLikes.setInt(1, rPostList.getInt("postId"));
                                    ResultSet rLikes = sLikes.executeQuery();
                                    rLikes.next();
                                    numberOfLikes = rLikes.getInt("numberOfLikes");
                                } catch (SQLException eNine) {
                                    try {
                                        c.rollback();
                                        return Result.fatal("!!!");
                                    } catch (SQLException eTen) {
                                        return Result.fatal("Rolling back failed !");
                                    }
                                }
                                posts.add(new PostView(forumId, topicId, postNumber, personName, username,
                                text, postedAt, numberOfLikes));
                            }
                            c.commit();
                            return Result.success(new TopicView(forumId, topicId, forumName, topicTitle, posts));
                        } catch (SQLException eSeven) {
                            try {
                                c.rollback();
                                return Result.fatal("Can not get topic, database rolled back One !");
                            } catch (SQLException eEight) {
                                return Result.fatal("Rolling back failed !");
                            }
                        }
                    } catch (SQLException eFive) {
                        try {
                            c.rollback();
                            return Result.fatal("Can not get topic, database rolled back Two !");
                        } catch (SQLException eSix) {
                            return Result.fatal("Rolling back failed !");
                        }
                    }

                } catch (SQLException eOne) {
                    try {
                        c.rollback();
                        return Result.fatal("Can not get topic, database rolled back Three !");
                    } catch (SQLException eTwo) {
                        return Result.fatal("Rolling back failed !");
                    }
                }
            }
        } catch (SQLException eThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get topic, database rolled back Four !");
            } catch(SQLException eFour) {
                return Result.fatal("Rolling back failed !");
            }
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /* B.2 */

    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
        ArrayList<AdvancedForumSummaryView> forums = new ArrayList<>();
        ArrayList<Integer> forumIds = new ArrayList<>();
        ArrayList<String> forumTitles = new ArrayList<>();
        ArrayList<TopicSummaryView> lastTopics = new ArrayList<>();
        HashMap<Integer, Integer> topicAndLikes = new HashMap<>();

        String qForums = "SELECT id as forumId, title as forumTitle FROM SimpleForum;";
        try (PreparedStatement sForums = c.prepareStatement(qForums)) {
            ResultSet rForums = sForums.executeQuery();
            while(rForums.next()) {
                forumIds.add(new Integer(rForums.getInt("forumId")));
                forumTitles.add(new String(rForums.getString("forumTitle")));
            }
            c.commit();
        } catch (SQLException eOne) {
            try {
                c.rollback();
                return Result.fatal("Can not get advanced forums, database rolled back !");
            } catch (SQLException eTwo) {
                return Result.fatal("Rolling back failed !");
            }
        }
        String drop_someView = "DROP VIEW IF EXISTS someView;";
        String drop_fJoinT = "DROP VIEW IF EXISTS fJoinT;";
        String drop_tJoinP = "DROP VIEW IF EXISTS tJoinP;";
        String drop_pJoinTp = "DROP VIEW IF EXISTS pJoinTp;";
        String drop_lpJoinTP = "DROP VIEW IF EXISTS lpJoinTP;";
        String drop_pJoinTp_02 = "DROP VIEW IF EXISTS pJoinTp_02;";
        String drop_fpJoinTP = "DROP VIEW IF EXISTS fpJoinTP;";
        String drop_no = "DROP VIEW IF EXISTS no;";
        String drop_like = "DROP VIEW IF EXISTS likeTopic;";


        String added = "CREATE VIEW someView as (SELECT MAX(t.id) as lastTopicId, " +
        "f.id as forumId " +
        "FROM SimpleForum as f INNER JOIN Topic as t ON t.forum = f.id " +
        "GROUP BY forumId);";

        //forum, lastTopicId
        String forum_Join_TopicView = "CREATE VIEW fJoinT AS (SELECT t1.lastTopicId as lastTopicId, t1.forumId as forumId, t2.name as lastTopicTitle " +
        "FROM someView as t1 INNER JOIN Topic as t2 ON t1.lastTopicId = t2.id);";

        //topic, lastPostId, firstPostId, related time
        String topicAndFirstPostIdAndLastPostId = "CREATE VIEW tJoinP AS (SELECT t.id as topicId, " +
        "MIN(p.atTime) as created, " +
        "MAX(p.atTime) as lastPostTime, " +
        "MAX(p.id) as lastPostId, " +
        "MIN(p.id) as firstPostId, " +
        "COUNT(p.id) as numberOfPosts " +
        "FROM Topic as t INNER JOIN " +
        "Post as p WHERE p.topic = t.id " +
        "GROUP BY topicId);";

        //topic， lastPost作者id
        String topicAndLastPostAuthorId = "CREATE VIEW pJoinTp AS (SELECT p.author as author, " +
        "tp.topicId as topicId " +
        "FROM Post as p INNER JOIN tJoinP as tp WHERE " +
        "p.id = tp.lastPostId);";

        //topic, lastPost作者姓名
        String topicAndLastPostAuthorName = "CREATE VIEW lpJoinTP AS (SELECT pe.name as lastPostName, " +
        "ptp.topicId as topicId FROM pJoinTp as ptp INNER JOIN " +
        "Person as pe WHERE ptp.author = pe.id);";

        //topic, firstPost作者Id
        String topicAndFirstPostAuthorId = "CREATE VIEW pJoinTp_02 AS (SELECT p.author as authorlastTopicId, " +
        "tp.topicId as topicId " +
        "FROM Post as p INNER JOIN tJoinP as tp WHERE " +
        "p.id = tp.firstPostId);";

        //topic, fisrtPost作者info
        String topicAndFirstPostAuthorInfo = "CREATE VIEW fpJoinTP as (SELECT pe.name as authorName, " +
        "pe.username as authorUsername, ftp.topicId as topicId " +
        "FROM Person as pe INNER JOIN " +
        "pJoinTp_02 as ftp ON ftp.authorlastTopicId = pe.id);";

        //topics no one like

        String topicNoOneLike = "SELECT ft.lastTopicId as topic " +
        "FROM fJoinT as ft WHERE ft.lastTopicId NOT IN " +
        "(SELECT DISTINCT topic FROM Favor);";

        //topic, number of Likers
        String topicAndNumberOfLikers = "SELECT f.topic as topic, COUNT(*) as numberOfLikes FROM " +
        "Favor as f GROUP BY topic;";


        String bigTable = "SELECT ft.lastTopicId as lastTopicId, ft.forumId as forumId, ft.lastTopicTitle as lastTopicTitle, " +
        "tp.numberOfPosts as numberOfPosts, tp.created as created, tp.lastPostTime as lastPostTime, " +
        "lptp.lastPostName as lastPostName, fptp.authorName as authorName, fptp.authorUsername as authorUsername " +
        "FROM fJoinT as ft INNER JOIN tJoinP as tp ON tp.topicId = ft.lastTopicId INNER JOIN " +
        "lpJoinTP as lptp ON lptp.topicId = ft.lastTopicId INNER JOIN " +
        "fpJoinTP as fptp ON fptp.topicId = ft.lastTopicId;";

        try (PreparedStatement sTopicAndNumberOfLikers = c.prepareStatement(topicAndNumberOfLikers)) {
            ResultSet rTopicAndNumberOfLikers = sTopicAndNumberOfLikers.executeQuery();
            while(rTopicAndNumberOfLikers.next()) {
                topicAndLikes.put(rTopicAndNumberOfLikers.getInt("topic"), rTopicAndNumberOfLikers.getInt("numberOfLikes"));
            }
            c.commit();
        } catch (SQLException eeOne) {
            try {
                c.rollback();
                return Result.fatal("!");
            } catch (SQLException eeTwo) {
                return Result.fatal("!!");
            }
        }


        try (PreparedStatement sZero = c.prepareStatement(drop_someView)) {
            sZero.executeQuery();
            c.commit();
        } catch (SQLException eeThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get advanced forums, database rolled back ***!");
            } catch (SQLException eeFour) {
                return Result.fatal("Rolling back failed !");
            }
        }


        //1
        try (PreparedStatement sOne = c.prepareStatement(drop_fJoinT)) {
            sOne.executeQuery();
            c.commit();
        } catch (SQLException eeThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get advanced forums, database rolled back ***!");
            } catch (SQLException eeFour) {
                return Result.fatal("Rolling back failed !");
            }
        }

        //2
        try (PreparedStatement sTwo = c.prepareStatement(drop_tJoinP)) {
            sTwo.executeQuery();
            c.commit();
        } catch (SQLException eeThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get advanced fortopicAndLastPostAuthorIdums, database rolled back ***!");
            } catch (SQLException eeFour) {
                return Result.fatal("Rolling back failed !");
            }
        }

        //3
        try (PreparedStatement sThree = c.prepareStatement(drop_pJoinTp)) {
            sThree.executeQuery();
            c.commit();
        } catch (SQLException eeThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get advanced forums, database rolled back ***!");
            } catch (SQLException eeFour) {
                return Result.fatal("Rolling back failed !");
            }
        }

        //4
        try (PreparedStatement sFour = c.prepareStatement(drop_lpJoinTP)) {
            sFour.executeQuery();
            c.commit();
        } catch (SQLException eeThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get advanced forums, database rolled back ***!");
            } catch (SQLException eeFour) {
                return Result.fatal("Rolling back failed !");
            }
        }

        //5
        try (PreparedStatement sFive = c.prepareStatement(drop_pJoinTp_02)) {
            sFive.executeQuery();
            c.commit();
        } catch (SQLException eeThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get advanced forums, database rolled back ***!");
            } catch (SQLException eeFour) {
                return Result.fatal("Rolling back failed !");
            }
        }

        //6
        try (PreparedStatement sSix = c.prepareStatement(drop_fpJoinTP)) {
            sSix.executeQuery();
            c.commit();
        } catch (SQLException eeThree) {
            try {
                c.rollback();
                return Result.fatal("Can not get advanced forums, database rolled back ***!");
            } catch (SQLException eeFour) {
                return Result.fatal("Rolling back failed !");
            }
        }

        try (PreparedStatement sqZero = c.prepareStatement(added)) {
            sqZero.executeQuery();
            try (PreparedStatement sqOne = c.prepareStatement(forum_Join_TopicView)) {
                sqOne.executeQuery();
                try (PreparedStatement sNoOneLike = c.prepareStatement(topicNoOneLike)) {
                    ResultSet rNoOneLike = sNoOneLike.executeQuery();
                    while(rNoOneLike.next()) {
                        topicAndLikes.put(rNoOneLike.getInt("topic"), 0);
                    }
                } catch (SQLException eeOne) {
                    try {
                        c.rollback();
                        return Result.fatal("!***");
                    } catch (SQLException eeTwo) {
                        return Result.fatal("!!");
                    }
                }
                try (PreparedStatement sqTwo = c.prepareStatement(topicAndFirstPostIdAndLastPostId)) {
                    sqTwo.executeQuery();
                    try (PreparedStatement sqThree = c.prepareStatement(topicAndLastPostAuthorId)) {
                        sqThree.executeQuery();
                        try (PreparedStatement sqFour = c.prepareStatement(topicAndLastPostAuthorName)) {
                            sqFour.executeQuery();
                            try (PreparedStatement sqFive = c.prepareStatement(topicAndFirstPostAuthorId)) {
                                sqFive.executeQuery();
                                try (PreparedStatement sqSix = c.prepareStatement(topicAndFirstPostAuthorInfo)) {
                                    sqSix.executeQuery();
                                    try (PreparedStatement sBigTable = c.prepareStatement(bigTable)) {
                                        ResultSet rTopics = sBigTable.executeQuery();
                                        //if(!rTopics.next()) {return Result.fatal("xixixi");}
                                        while(rTopics.next()) {
                                            int numberOfLikes =  topicAndLikes.get(rTopics.getInt("lastTopicId"));
                                            lastTopics.add(new TopicSummaryView(rTopics.getInt("lastTopicId"), rTopics.getInt("forumId"),
                                            rTopics.getString("lastTopicTitle"), rTopics.getInt("numberOfPosts"), rTopics.getString("created"),
                                            rTopics.getString("lastPostTime"), rTopics.getString("lastPostName"), numberOfLikes,
                                            rTopics.getString("authorName"), rTopics.getString("authorUsername")));
                                        }
                                        Iterator<Integer> itForumIds = forumIds.iterator();
                                        Iterator<String> itForumTitles = forumTitles.iterator();
                                        Iterator<TopicSummaryView> itLastTopics = lastTopics.iterator();
                                        //if(!itLastTopics.hasNext()) {return Result.fatal("hahaha");}
                                        while(itLastTopics.hasNext()) {
                                            forums.add(new AdvancedForumSummaryView(itForumIds.next(), itForumTitles.next(), itLastTopics.next()));
                                        }
                                        c.commit();
                                        return Result.success(forums);
                                    } catch (SQLException eOne) {
                                        try {
                                            c.rollback();
                                            return Result.fatal("Can not get advanced forums, database rolled back ***!");
                                        } catch (SQLException eTwo) {
                                            return Result.fatal("Rolling back failed !");
                                        }
                                    }
                                } catch (SQLException eeThree6) {
                                    try {
                                        c.rollback();
                                        return Result.fatal("Can not get advanced forums, database rolled back ***!");
                                    } catch (SQLException eeFour6) {
                                        return Result.fatal("Rolling back failed !");
                                    }
                                }
                            } catch (SQLException eeThree5) {
                                try {
                                    c.rollback();
                                    return Result.fatal("Can not get advanced forums, database rolled back ***!");
                                } catch (SQLException eeFour5) {
                                    return Result.fatal("Rolling back failed !");
                                }
                            }
                        } catch (SQLException eeThree4) {
                            try {
                                c.rollback();
                                return Result.fatal("Can not get advanced forums, database rolled back ***!");
                            } catch (SQLException eeFour4) {
                                return Result.fatal("Rolling back failed !");
                            }
                        }
                    } catch (SQLException eeThree3) {
                        try {
                            c.rollback();
                            return Result.fatal("Can not get advanced forums, database rolled back ***!");
                        } catch (SQLException eeFour3) {
                            return Result.fatal("Rolling back failed !");
                        }
                    }
                } catch (SQLException eeThree2) {
                    try {
                        c.rollback();
                        return Result.fatal("Can not get advanced forums, database rolled back ***!");
                    } catch (SQLException eeFour2) {
                        return Result.fatal("Rolling back failed !");
                    }
                }
            } catch (SQLException eeThree1) {
                try {
                    c.rollback();
                    return Result.fatal("Can not get advanced forums, database rolled back ***!");
                } catch (SQLException eeFour1) {
                    return Result.fatal("Rolling back failed !");
                }
            }
        } catch (SQLException eeThree0) {
            try {
                c.rollback();
                return Result.fatal("!!!!!");
            } catch (SQLException eeFour0) {
                return Result.fatal("Rolling back failed !");
            }
        }

        //q1

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<AdvancedPersonView> getAdvancedPersonView(String username) {
        return Result.failure("!");
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Result<AdvancedForumView> getAdvancedForum(int id) {
        return Result.failure("!");
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
