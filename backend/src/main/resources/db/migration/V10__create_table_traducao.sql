CREATE TABLE traducao (
                          id             BIGSERIAL PRIMARY KEY,
                          origem         VARCHAR(500) NOT NULL,
                          idioma_origem  VARCHAR(10)  NOT NULL,
                          idioma_destino VARCHAR(10)  NOT NULL,
                          traducao       VARCHAR(500) NOT NULL,
                          CONSTRAINT uk_traducao UNIQUE (origem, idioma_origem, idioma_destino)
);

-- Alimentos comuns já prontos (PT -> EN), sem gastar a API.
-- A coluna origem guarda o texto JÁ normalizado: minúsculo e sem acento.
INSERT INTO traducao (origem, idioma_origem, idioma_destino, traducao) VALUES
                                                                           ('maca',     'pt-BR', 'en', 'apple'),
                                                                           ('banana',   'pt-BR', 'en', 'banana'),
                                                                           ('abacaxi',  'pt-BR', 'en', 'pineapple'),
                                                                           ('mexerica', 'pt-BR', 'en', 'tangerine'),
                                                                           ('mandioca', 'pt-BR', 'en', 'cassava'),
                                                                           ('aipim',    'pt-BR', 'en', 'cassava'),
                                                                           ('arroz',    'pt-BR', 'en', 'rice'),
                                                                           ('feijao',   'pt-BR', 'en', 'beans'),
                                                                           ('ovo',      'pt-BR', 'en', 'egg');