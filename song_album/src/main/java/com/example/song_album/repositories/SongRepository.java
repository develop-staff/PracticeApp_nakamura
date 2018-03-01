package com.example.song_album.repositories;


import com.example.song_album.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song,Long>{

    public Optional<Song>findById(Long name);
    public Optional<Song>deleteById(Long name);
}

