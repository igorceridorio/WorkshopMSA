
# Microsserviços com Java Spring


Este repositório tem como objetivo mostrar como podemos evoluir um projeto feito em [Java Spring](https://spring.io/) aplicando conceitos da arquitetura de microsserviços utilizando [Spring Cloud](https://projects.spring.io/spring-cloud/). O projeto é incremental, e o repositório está organizado de acordo com estas etapas.

Todos os projetos Java deste repositório são projetos Maven e foram criados com a ferramenta [SPRING INITIALIZR](https://start.spring.io/). Portanto você pode importá-los como projetos Maven a partir de sua IDE desejada. Utilizei Eclipse para desenvolvimento.

As instruções abaixo se baseiam no estado incial dos projetos exemplo, presentes no diretório [1.projeto-base](https://github.com/igorceridorio/WorkshopMSA/tree/master/1.projeto-base).

## Sumário
1. [Entendendo o projeto exemplo](#projeto-exemplo)
2. [Load balancing ](#ribbon)
3. [Naming service](#eureka)
4. [Tracing](#sleuth)
5. [Distributed tracing server](#zipkin)
6. [Fault tolerance](#hystrix)

## Entendendo o projeto exemplo <a name="projeto-exemplo"></a>

Dentro do diretório [1.projeto-base](https://github.com/igorceridorio/WorkshopMSA/tree/master/1.projeto-base) você irá perceber que existem dois projetos: **emprestimo-microservice** e **taxa-juros-microservice**. Eles são a base para nosso tutorial.

### taxa-juros-microservice

Este microsserviço tem a função de obter informações sobre juros a/m  de alguns produtos vindos de uma tabela de um repositório de dados. Tenha em mente que o foco deste tutorial é discutirmos os componentes da arquitetura de microsserviços, por isso trabalhamos com exemplos simples. O banco de dados utilizado é um H2 em memória, criado quando uma instância do microsserviço de taxa de juros é iniciada.

Possui dois  endpoints, um que retorna a taxa de juros de todos os produtos cadastrados e outro que retorna a taxa de juros de um produto específico dado seu ID.

```
GET /taxajuros
GET /{idProduto}
```

### emprestimo-microservice

A função deste microsserviço é calcular um plano de empréstimo dado um determinado produto, o valor solicitado e a quantidade de parcelas desejadas. O microsserviço de empréstimos busca a taxa de juros do produto desejado junto ao microsserviço taxa-juros-microservice e calcula o valor final do empréstimo. Possui um único endpoint:

```
GET /calculo/{idProduto}/{valor}/{parcelas}
```

Segue um diagrama com a representação do projeto base:

![Diagrama do projeto base](https://i.imgur.com/2avN0oc.png)

Neste momento a configuração de endereços da aplicação é a seguinte:

|Serviço|Porta(s)|
|-|-|
|emprestimo-microservice|8100|
|taxa-juros-microservice|8000|

Segue um exemplo de chamada ao serviço de empréstimos:

```
http://localhost:8100/emprestimos/calculo/15/1000/24
```

E sua resposta:

```json
{
	"parcelas": 24,
	"valorSolicitado": 1000,
	"valorParcela": 45.36,
	"valorFinal": 1088.64,
	"idProduto": 15,
	"jurosAm": 8.29,
	"porta": 8000
}
```