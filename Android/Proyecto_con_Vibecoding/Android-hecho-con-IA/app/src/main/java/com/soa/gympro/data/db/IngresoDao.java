package com.soa.gympro.data.db;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface IngresoDao {
    @Insert
    void insertar(Ingreso ingreso);
}
