alter table registro_consumo
add column usuario_id bigint not null,

add constraint fk_registro_usuario
    foreign key (usuario_id)
        references usuario(id)