create table porcao_alimento(
    id Bigserial primary key,
    alimento_id bigint not null,
    quantidade double precision,
    peso_grama double precision,
    unidada_tipo varchar(100),
    descricao varchar(200),

    constraint fl_porcao_alimento_alimento
                            foreign key (alimento_id) references alimentos (id)
                            on delete cascade
);