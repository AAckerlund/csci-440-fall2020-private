package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Album extends Model {

    Long albumId;
    Long artistId;
    String title;

    public Album() {
    }

    private Album(ResultSet results) throws SQLException {
        title = results.getString("Title");
        albumId = results.getLong("AlbumId");
        artistId = results.getLong("ArtistId");
    }

    public Artist getArtist() {
        return Artist.find(artistId);
    }

    public void setArtist(Artist artist) {
        artistId = artist.getArtistId();
    }

    public List<Track> getTracks() {
        return Track.forAlbum(albumId);
    }

    public Long getAlbumId() {
        if(albumId != null)
            return albumId;
        try(Connection conn = DB.connect();
        PreparedStatement stmt = conn.prepareStatement("SELECT * from albums WHERE Title=?"))
        {
            stmt.setString(1, title);
            ResultSet results = stmt.executeQuery();
            if(results.isClosed())
                return null;
            return results.getLong("AlbumId");
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void setAlbum(Album album) {
        this.albumId = album.getAlbumId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public Long getArtistId() {
        return artistId;
    }
    
    public void setArtistId(Long id)
    {
        artistId = id;
    }

    public static List<Album> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Album> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM albums LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count*(page-1));
            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Album find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE AlbumId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Album(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Album> getForArtist(Long artistId) {
        // TODO implement
        return Collections.emptyList();
    }
    
    public boolean create()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO albums(Title, ArtistId) VALUES(?, ?)")) {
        stmt.setString(1, title);
        stmt.setLong(2, artistId);
        return stmt.executeUpdate() > 0;
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public boolean update()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("UPDATE albums SET Title=? WHERE AlbumId=?")) {
            stmt.setString(1, title);
            stmt.setLong(2, albumId);
            return stmt.execute();
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void delete()
    {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM albums WHERE albumID=?")) {
            stmt.setLong(1, this.getAlbumId());
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
    
    public boolean verify()
    {
        _errors.clear();
        if(title == null || title.equals(""))
            addError("No Title Found");
        if(artistId == null || artistId < 0)
            addError("Invalid Artist Id");
        return !hasErrors();
    }
}