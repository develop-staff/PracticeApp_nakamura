package com.example.songs_album.repositories;

import com.example.songs_album.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song,Long>{

    public Optional<Song>findById(Long name);
}
