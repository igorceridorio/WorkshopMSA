
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
|emprestimo-microservice|`8100`|
|taxa-juros-microservice|`8000`|

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

Segue um diagrama com a representação do projeto atualizada:

![Diagrama do projeto atualizado](https://i.imgur.com/qPm1dOl.png)

Neste momento a configuração de endereços da aplicação é a seguinte:

|Serviço|Porta(s)|
|-|-|
|emprestimo-microservice|`8100`|
|taxa-juros-microservice|`8000, 8001, 8002`|

Se uma instância cair ou alguma outra for adicionada, como o sistema se comportaria? Ele não seria capaz, neste momento, de detectar alterações nas instâncias. para isso precisamos de um **naming server** e de um **server discovery**, utilizaremos o [Eureka](https://github.com/Netflix/eureka).

## Eureka: Naming service <a name="eureka"></a>

Os microsserviços precisam saber em quais portas e IPs os demais microsserviços podem ser encontrados. Desta maneira, se instâncias do microsserviço de taxa de juros caírem ou forem criadas, o microsserviço de empréstimos saberá quais dessas instâncias são ativas, quantas existem, e para quais endereços ele pode rotear suas chamadas. 

Através das etapas abaixo vamos adicionar o naming server ao nosso ecossistema e configurar os microsserviços de taxa de juros e empréstimo para que eles se registrem no servidor Eureka.

Dentro do diretório [1.projeto-base](https://github.com/igorceridorio/WorkshopMSA/tree/master/1.projeto-base) existe um terceiro projeto chamado **eureka-naming-server**. Ele será o nosso *naming server*, ou seja, é nele que os microsserviços do nosso sistema irão se registrar. O balanceador de carga que configuramos irá até ele para consultar o endereço das instâncias do microsserviço de taxa de juros disponíveis, esse mecanimo é conhecido por *service discovery*. 

Importe o projeto **eureka-naming-server** do diretório e siga os seguintes passos:

1. **[EurekaNamingServerApplication.java]** Habilite o servidor Eureka em seu projeto. Adicione a seguinte annotation na main class:

	```java
	@EnableEurekaServer
	```
2. **[application.yml - eureka-naming-server]** Precisamos agora definir algumas configurações: o nome desta aplicação, em qual porta desejamos rodar nosso *naming server*, e proteger nosso servidor para que ele não seja descoberto por outras instâncias e que não busque informações de outros *naming servers*.

	```yml
	spring.application.name: eureka-naming-server
	server.port: 8761

	# não desejamos que o naming server seja descoberto por outras instâncias
	eureka.client.register-with-eureka: false

	# não desejamos que o naming server busque informações de outro naming server
	eureka.client.fetch-registry: false
	```
3. **[pom.xml - taxa-juros-microservice]** Adicione o **Eureka Client** na lista de dependências do microsserviço de taxa de juros.

	```xml
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
	</dependency>
	```
4.  **[TaxaJurosMicroserviceApplication.java]** Na main class da aplicação, adicione a seguinte annotation para permitir que o microsserviço seja "descoberto" pelo naming server.

	```java
	@EnableDiscoveryClient
	```

5. **[application.yml - taxa-juros-microservice]** Adicione a localização do servidor Eureka para que seja possível que o microsseriço se registre junto ao *naming server*.

	```yml
	eureka.client.serviceUrl.defaultZone: http://localhost:8761/eureka/
	```
6. **[pom.xml - emprestimo-microservice]** Adicione o **Eureka Client** na lista de dependências do microsserviço de empréstimos.

	```xml
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
	</dependency>
	```
7.  **[EmprestimoMicroserviceApplication.java]** Na main class da aplicação, adicione a seguinte annotation para permitir que o microsserviço seja "descoberto" pelo naming server.

	```java
	@EnableDiscoveryClient
	```
	
8. **[application.yml - emprestimo-microservice]** Adicione a localização do servidor Eureka para que seja possível que o microsserviço se registre junto ao *naming server*. Comente ou remova a linha com a lista de servidores que antes era usada pelo Ribbon, afinal agora o Eureka é o responsável por manter em seus registros a lista de instâncias ativas do microsseriço de taxa de juros.

	```yml
	eureka.client.serviceUrl.defaultZone: http://localhost:8761/eureka/
	#taxa-juros-microservice.ribbon.listOfServers: http://localhost:8000, http://localhost:8001, http://localhost:8002
	```
Para testarmos a aplicação, suba nesta ordem:

1. eureka-naming-server
2. taxa-juros-microservice (`8000`, `8001` e `8002`) 
3. emprestimo-microservice
	
O *naming server* possui uma interface onde podemos verificar informações de quais instâncias estão de pé, bem como nomes e endereços das mesmas. Com a aplicação de pé basta acessar: http://localhost:8761.

Segue um diagrama com a representação do projeto atualizada:

![Diagrama do projeto atualizado](https://i.imgur.com/J1D5uKQ.png)

Neste momento a configuração de endereços da aplicação é a seguinte:

|Serviço|Porta(s)|
|-|-|
|emprestimo-microservice|`8100`|
|taxa-juros-microservice|`8000, 8001, 8002`|
|eureka-naming-server|`8761`|

Com várias instâncias de pé, como debuggar e entender o que aconteceu com determinado request quando necessário? Precisamos de **tracing**, e para isso utilizaremos o [Sleuth](https://cloud.spring.io/spring-cloud-sleuth/).

## Sleuth: Tracing <a name="sleuth"></a>

No momento atual, os controllers das aplicaçãoes de taxa de juros e de empréstimos logam os responses dos requests ao seus endpoints. Você pode conferir isto dentro dos arquivos `EmprestimoController.java` e `TaxaJurosController.java`. No entanto, são apenas logs isolados, vamos agora transformar esses logs em rastros (ou **traces**). Com um trace, adicionamos um identificador único ao request, e este identificador se propaga até o serviço seguinte. No caso, o identificador gerado no emprestimo-microservice se propaga até o taxa-juros-microservice. Com isso, conseguimos analisar o caminho exato de um request, e caso ocorra algum problema com ele, conseguimos identificar o ponto onde isso ocorreu.

Para adicionar tracing ao projeto basta seguir os passos abaixo:

1. **[pom.xml - taxa-juros-microservice]** Adicione o **Starter Sleuth** na lista de dependências do microsserviço de taxa de juros.

	```xml
	<dependency>
	        <groupId>org.springframework.cloud</groupId>
	        <artifactId>spring-cloud-starter-sleuth</artifactId>
	</dependency>
	```
2. **[application.yml - taxa-juros-microservice]** Defina a porcentagem de tracing que você deseja gerar.

	```yml
	# logará 100% dos tracings  
	sleuth.sampler.probability: 1.0
	```
3. **[TaxaJurosMicroserviceApplication.java]** Adicione um `@Bean` que configura um `Sampler` para `ALWAYS_SAMPLE`.  Por padrão  o **Spring Cloud Sleuth** configura todos os seus traces como *não exportáveis*. Isto quer dizer que os traces aparecem no log, mas não aparecem em nenhum armazenamento remoto. Para poder exportar nossos traces, devemos declarar este bean. Ele será útil no próximo passo.

	```java
	@Bean  
	public Sampler defaultSampler() {  
		return Sampler.ALWAYS_SAMPLE;  
	}
	```
4. **[pom.xml - emprestimo-microservice]** Adicione o **Starter Sleuth** na lista de dependências do microsserviço de empréstimos.

	```xml
	<dependency>
	        <groupId>org.springframework.cloud</groupId>
	        <artifactId>spring-cloud-starter-sleuth</artifactId>
	</dependency>
	```
5. **[application.yml - emprestimo-microservice]** Defina a porcentagem de tracing que você deseja gerar.

	```yml
	# logará 100% dos tracings  
	sleuth.sampler.probability: 1.0
	```
6. **[EmprestimoMicroserviceApplication.java]** Adicione um `@Bean` que configura um `Sampler` para `ALWAYS_SAMPLE`.  

	```java
	@Bean  
	public Sampler defaultSampler() {  
		return Sampler.ALWAYS_SAMPLE;  
	}
	```
Ao executar sua aplicação, você notará que para cada vez que um de seus endpoints é acessado, no console, terá algo como:

`[taxa-juros-microservice,2dc2df56169de8ac,67df8567c1afeb4a,false]` 

Este é um **trace**! O primeiro campo é o nome do serviço no qual a requisição está sendo executada, o segundo é o **trace-id**, identificador único do request, o terceiro é o **span-id**, identificador único da operação, e o último é um booleano que indica se o trace está ou não sendo exportado para algum servidor.

Os tracings ajudam, mas ainda é difícil visualizar qual caminho um request percorreu. Para isso, precisamos de um **Tracing Server**. Em nosso projeto, utilizaremos o [Zipkin](https://zipkin.io/).