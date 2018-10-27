
# Microsserviços com Java Spring


Este repositório tem como objetivo mostrar como podemos evoluir um projeto feito em [Java Spring](https://spring.io/) aplicando conceitos da arquitetura de microsserviços utilizando [Spring Cloud](https://projects.spring.io/spring-cloud/). O projeto é incremental, e o repositório está organizado de acordo com estas etapas.

A ideia deste projeto vem de um artigo que escrevi com a base teórica explicando a estrutura e organização de um ecossistema de microsserviços. O artigo pode ser encontrado [aqui!](https://www.opus-software.com.br/arquitetura-de-microsservicos/)

Todos os projetos Java deste repositório são projetos Maven e foram criados com a ferramenta [SPRING INITIALIZR](https://start.spring.io/). Portanto você pode importá-los como projetos Maven a partir de sua IDE desejada. Utilizei Eclipse para desenvolvimento.

As instruções abaixo se baseiam no estado incial dos projetos exemplo, presentes no diretório [1.projeto-base](https://github.com/igorceridorio/WorkshopMSA/tree/master/1.projeto-base).

## Sumário
1. [Entendendo o projeto exemplo](#projeto-exemplo)
2. [Ribbon: Load balancing ](#ribbon)
3. [Eureka: Naming service](#eureka)
4. [Sleuth: Tracing](#sleuth)
5. [Zipikin: Distributed tracing server](#zipkin)
6. [Hystrix: Fault tolerance](#hystrix)

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
Em seguida, vamos adicionar o [Ribbon](https://github.com/Netflix/ribbon/wiki) ao nosso microsserviço de empréstimos, para que as requisições que chegam ao serviço de taxa de juros sejam balanceadas.

## Ribbon: Load balancing <a name="ribbon"></a>

Balanceamento de carga é necessário para que um serviço possa escalar independentemente dos demais. Para isso, é necessário que requisições para um microsserviço possam ser distribuídas para as múltiplas instâncias do mesmo. É o que faremos agora com o nosso projeto, basta seguir o passo a passo abaixo:

1. **[pom.xml - emprestimo-microservice]** Adicione o Ribbon na lista de dependências do microsserviço de empréstimos.

	```xml
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-ribbon</artifactId>
		<version>1.4.5.RELEASE</version>
	</dependency>
	```

2. **[TaxaJurosServiceProxy.java]** Habilite o Ribbon dentro da interface de acesso ao microsserviço de taxas de juros.

* Remover:
	```java
	@FeignClient(name = "taxa-juros-microservice", url = "http://localhost:8000")
	```
* Adicionar:
	```java
	@FeignClient(name = "taxa-juros-microservice")  
	@RibbonClient(name = "taxa-juros-microservice")
	```
3. **[application.yml - emprestimo-microservice]** Agora é necessário definir a lista de URLs onde o serviço de taxa de juros está disponível para acesso. Para isso, adicione:

	```yml
	taxa-juros-microservice.ribbon.listOfServers: http://localhost:8000, http://localhost:8001, http://localhost:8002
	```
A partir de agora, o proxy não será mais o responsável por apontar o endereço do microsserviço de taxa de juros, Ribbon terá a tarefa de encontrar este endereço. No entanto, ele não faz isso "magicamente", Por isso definimos no `application.yml` do microsserviço de empréstimos uma lista com endereços de onde podemos encontrar instâncias ativas do microsserviço de taxa de juros. 

Para testar suba a sua aplicação de taxa de juros nas três portas informadas acima: `8000, 8001` e `8002`. Você pode fazer isso facilmente passando `-Dserver.port=8001` e `-Dserver.port=8002` como parâmetros de execução da sua aplicação, ou definindo os valores das variáveis `SERVER_PORT` no *Run Configurations* do Eclipse, por exemplo. A configuração padrão já irá subir a primeira execução na porta `8000`.

A partir de agora, ao fazer uma requisição de empréstimos você irá perceber na resposta que o atributo `porta` passará a vir cada hora com um valor diferente, alternando entre as instâncias do microsserviço de taxa de juros que estão de pé.
