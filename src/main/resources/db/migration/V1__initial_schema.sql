CREATE TABLE cliente (
    cod_cliente BIGSERIAL PRIMARY KEY,
    nme_cliente VARCHAR(150) NOT NULL,
    dta_nascimento DATE NOT NULL,
    nro_cpf VARCHAR(11) UNIQUE NOT NULL,
    dta_criacao TIMESTAMP DEFAULT NOW()
);

CREATE TABLE endereco (
    cod_endereco BIGSERIAL PRIMARY KEY,
    nme_logradouro VARCHAR(200) NOT NULL,
    nme_bairro VARCHAR(100),
    nro_cep VARCHAR(8) NOT NULL,
    nme_cidade VARCHAR(100) NOT NULL,
    nme_estado CHAR(2) NOT NULL
);

CREATE TABLE ocorrencia (
    cod_ocorrencia BIGSERIAL PRIMARY KEY,
    cod_cliente BIGINT REFERENCES cliente(cod_cliente),
    cod_endereco BIGINT REFERENCES endereco(cod_endereco),
    dta_ocorrencia TIMESTAMP NOT NULL DEFAULT NOW(),
    sta_ocorrencia VARCHAR(10) NOT NULL DEFAULT 'ATIVA'
        CHECK (sta_ocorrencia IN ('ATIVA','FINALIZADA'))
);

CREATE TABLE foto_ocorrencia (
    cod_foto_ocorrencia BIGSERIAL PRIMARY KEY,
    cod_ocorrencia BIGINT REFERENCES ocorrencia(cod_ocorrencia) ON DELETE CASCADE,
    dta_criacao TIMESTAMP DEFAULT NOW(),
    dsc_path_bucket TEXT NOT NULL,
    dsc_hash TEXT
);