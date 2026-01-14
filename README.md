# Teste Técnico Backend Java Sênior – CARBIGDATA
**Dezembro 2025**  
**Candidato:** Andrei Lima  
**Projeto:** Sistema de ingestão de alta vazão de ocorrências com fotos (IoT/Câmeras de trânsito)


## Exemplo de utilização

curl -X POST http://localhost:8080/ocorrencias \
-H "Content-Type: multipart/form-data" \
-F 'dados={
"nmeCliente": "Andrei Lima",
"dtaNascimento": "1990-01-01",
"nroCpf": "12345678901",
"nmeLogradouro": "Avenida Paulista",
"nmeBairro": "Bela Vista",
"nroCep": "01310100",
"nmeCidade": "São Paulo",
"nmeEstado": "SP",
"dtaOcorrencia": "2025-12-10T10:00:00"
};type=application/json' \
-F 'foto=@/home/andrei/Downloads/foto.jpg'

## Como rodar

```bash
docker-compose up --build
sudo docker-compose up -d
