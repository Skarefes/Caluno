create table usuario (
    id Bigserial primary key,
    nome varchar(255) not null unique,
    senha varchar(255) not null
)