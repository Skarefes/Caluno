create table registro_consumo(
    id BIGSERIAL primary key,
    alimento_id BIGINT NOT NULL,
    quantidade DOUBLE PRECISION,
    unidade_tipo VARCHAR(50),
    peso_grama DOUBLE PRECISION,
    data TIMESTAMP not null,

    CONSTRAINT fk_registro_alimento
                             foreign key (alimento_id)
                             references alimentos(id)
)