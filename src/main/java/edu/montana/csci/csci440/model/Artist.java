package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Artist extends Model {

    Long artistId;
    String name;
    String originalName;

    public Artist() {
        originalName = name;
    }

    private Artist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        originalName = name;
        artistId = results.getLong("ArtistId");
    }

    public List<Album> getAlbums(){
        return Album.getForArtist(artistId);
    }

    public Long getArtistId() {
        try(Connection conn = DB.connect();
            PreparedStatement stmt = conn.prepareStatement("SELECT * from artists WHERE Name=?"))
        {
            stmt.setString(1, name);
            ResultSet results = stmt.executeQuery();
            if(results.isClosed())
                return null;
            return results.getLong("ArtistId");
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void setArtist(Artist artist) {
        this.artistId = artist.getArtistId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        originalName = this.name;
        this.name = name;
    }

    public static List<Artist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Artist> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM artists LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count*(page-1));
            ResultSet results = stmt.executeQuery();
            List<Artist> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Artist(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Artist find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM artists WHERE ArtistId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Artist(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
    
    public boolean create()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO artists(Name) VALUES(?)")) {
            stmt.setString(1, name);
            boolean finished = stmt.execute();
            artistId = getArtistId();
            originalName = name;
            return finished;
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public boolean update()
    {
        if(verify())
        {
            try(Connection conn = DB.connect(); PreparedStatement stmt = conn.prepareStatement("UPDATE artists SET Name=? WHERE ArtistId=? and Name=?"))
            {
                stmt.setString(1, name);
                stmt.setLong(2, artistId);
                stmt.setString(3, originalName);
                int num = stmt.executeUpdate();
                System.out.println(getName());
                System.out.println(num);
                return num > 0;
            }
            catch(SQLException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        else
            return false;
    }
    
    public boolean verify()
    {
        _errors.clear();
        if(name == null || name.equals(""))
            addError("No Title Found");
        return !hasErrors();
    }
}
