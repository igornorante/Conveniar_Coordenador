package com.example.conveniar_coordenador.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projetos")
public class ProjetoEntity {

    @PrimaryKey
    @NonNull
    public String idProjeto; // ID único vindo da API

    public String situacao;

    public boolean precisaAtencao; // True se for novo ou tiver mudado de status

    public String jsonOriginal; // Guarda o JSON inteiro para facilitar na hora de montar a tela

    // Construtor vazio exigido pelo Room
    public ProjetoEntity() {}
}
