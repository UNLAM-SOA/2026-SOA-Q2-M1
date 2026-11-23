package com.soa.gympro.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PagoDao {
    @Query("SELECT * FROM pagos WHERE dni = :dni ORDER BY fecha DESC")
    List<Pago> pagosDe(String dni);

    @Query("SELECT COUNT(*) FROM pagos WHERE dni = :dni")
    int cantidadDe(String dni);

    @Insert
    void insertar(List<Pago> pagos);
}
