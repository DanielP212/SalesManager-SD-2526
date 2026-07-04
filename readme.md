# 🏪 sd-sales-manager: Sistema Distribuído de Gestão e Análise de Vendas (Java)

Este é um projeto desenvolvido no âmbito da licenciatura em Engenharia Informática para a cadeira de Sistemas Distribuídos do 3º ano. Consiste num sistema cliente-servidor multithreading distribuído, desenvolvido em Java, para a gestão e análise de transações comerciais em tempo real. O sistema implementa um protocolo aplicacional binário próprio sobre sockets TCP, integrando um controlo estrito de concorrência para garantir consistência dos dados, persistência local otimizada em disco e um sistema assíncrono de notificações assentes no padrão Publisher-Subscriber.

**Para uma descrição completa do projeto consultar o [relatório](relatorio/relatorio.tex)

---

## ✨ Funcionalidades

✅ **Arquitetura Cliente-Servidor Nativa:** Comunicação TCP fiável e bidirecional através de `Socket` e `ServerSocket` isolando a interface do utilizador da lógica de negócio.

✅ **Protocolo Binário Customizado:** Desenho de um protocolo de rede próprio assente em estruturas de pacotes binários (`Packet` e `PacketHeader`), otimizando o tráfego de rede e a serialização.

✅ **Gestão Avançada de Concorrência:** Controlo estrito e *thread-safe* na manipulação de dados partilhados recorrendo a locks explícitos (`ReentrantLock`, `Condition` e `ReadWriteLock`).

✅ **Notificações Assíncronas:** Mecanismo reativo de subscrição de eventos (`Condition.await`/`signalAll`) para alertar os clientes sobre metas de vendas consecutivas ou sequenciais de produtos.

✅ **Persistência e Cache Otimizada:** Armazenamento em disco em formato binário `.dat` complementado por uma cache em memória (`workDaysCache`) com políticas concorrentes de despejo de dados históricos.

✅ **Autenticação e Permissões:** Controlo de acessos estruturado (Registo/Login) com suporte a sessões de utilizador e restrições administrativas para operações sensíveis (como avançar o dia de trabalho).


## 🎯 Tarefas desenvolvidas

### 🏗️ Arquitetura e Design do Sistema (Distribuído / Concorrente)

* **Desenho da Arquitetura Distribuída:** Implementação da infraestrutura de rede baseada em *sockets* TCP para suporte a múltiplos clientes em simultâneo.
* **Modelo Multithreading e Pool de Threads:** Desenvolvimento do ciclo de vida das threads servidoras (`RequestHandlerThread`) e clientes (`PendingRequestThread`), recorrendo a estratégias de pools dinâmicas (*busy/free*) para otimização de recursos.
* **Protocolo Aplicacional de Rede:** Conceção da estrutura de tráfego de rede através da criação de cabeçalhos e payloads binários tipificados (`PacketType`).
* **Sincronização Avançada:** Garantia de *Thread-Safety* em estruturas partilhadas críticas como o registo de utilizadores e cache de vendas através de exclusão mútua fina.

### 💻 Desenvolvimento Backend & Lógica de Negócio

* **Motor de Gestão de Vendas (`SalesManager`):** Codificação da lógica core para transações comerciais diárias e controlo coordenado da transição de estados de tempo (`advanceDay`).
* **Processamento Analítico de Dados:** Implementação de algoritmos de agregação estatística para calcular volumes, faturamentos, médias e máximos históricos por produto.
* **Mecanismo de Notificações Concorrentes e Sequenciais:** Desenvolvimento do sistema reativo assente no padrão *Publisher-Subscriber* para bloqueio e desbloqueio controlado de clientes com base em fluxos de venda (`ConcSaleNotification` e `SeqSaleNotification`).
* **Gestão de Autenticação e Permissões:** Criação do módulo de segurança para controlo de privilégios com base em perfis administrativos (`User.isAdmin`).

### 💾 Persistência de Dados & Serialização

* **Serialização Manual de Baixo Nível (`Encodable`):** Implementação de rotinas para conversão direta de tipos primitivos e strings para matrizes de bytes (`byte[]`), maximizando a performance da rede.
* **Persistência em Disco Customizada:** Criação de fluxos binários organizados por dia de trabalho através de `DataOutputStream` e `DataInputStream` buffered.
* **Mecanismo de Cache:** Arquitetura de uma cache local com indexação temporal (`TreeMap`) e algoritmos concorrentes para despejo de registos não utilizados (`makeRoomInCache`).

### 🖥️ Interface e Experiência do Cliente

* **Interface Textual Interativa (CLI):** Desenho e implementação da navegação em menu por consola assente no padrão de desenho **Composite/Command**.
* **Validação de Inputs:** Programação de rotinas robustas para leitura de streams do utilizador, prevenindo erros de conversão de dados.
* **Tratamento Assíncrono de Respostas:** Implementação do monitor de bloqueio local (`PendingRequest`) baseado em variáveis de condição para sincronizar pacotes de resposta com as janelas temporais corretas de pedidos.

### 🧪 Testes, Qualidade e Diagnóstico

* **Simulação de Testes Unitários:** Adaptação da arquitetura para suportar isolamento por instâncias de teste (`isTestInstance`), permitindo a injeção e captura de streams simulados.
* **Gerador de Dados de Teste (`FileCreator`):** Criação de um utilitário de teste automatizado para gerar grandes volumes de históricos comerciais fictícios em ficheiros `.dat`.
* **Logging de Tráfego:** Desenvolvimento de rotinas de diagnóstico textuais detalhadas para a monitorização contínua das pools de threads e integridade dos buffers de pacotes.

---

## 🛠️ Instalação e Compilação

O projeto foi desenvolvido em Java nativo e não requer gestores de dependências externos. Garante que tens o JDK 17 ou superior instalado.

```bash
# 1. Clonar o repositório
git clone [https://github.com/o-teu-utilizador/sd-sales-manager.git](https://github.com/o-teu-utilizador/sd-sales-manager.git)
cd sd-sales-manager

# 2. Criar diretório para os binários compilados
mkdir bin

# 3. Compilar todo o projeto Java
javac -d bin src/*.java src/client/*.java src/client/answers/*.java src/client/menu/*.java src/comms/*.java src/comms/common/*.java src/core/*.java src/core/base/*.java src/server/*.java src/server/requests/*.java src/test/*.java

```

## 🚀 Como Executar o Projeto

Garante que executas o servidor primeiro para que este fique à escuta de ligações na porta configurada (`12345`).

### 1. Iniciar o Servidor

Abra um terminal na raiz do projeto e execute:

```bash
java -cp bin ServerMain

```

*O servidor inicializará a base de dados de produtos em memória e criará o ficheiro binário para o dia de trabalho atual.*

### 2. Iniciar o Cliente

Num novo terminal, execute:

```bash
java -cp bin ClientMain

```

### O Fluxo de Utilização

1. **Autenticação:** O cliente iniciará no menu de Login. Podes usar as credenciais padrão de administração pré-carregadas (`admin` / `admin`) ou registar um novo utilizador através do `InputHandler`.
2. **Navegação:** Após o login bem-sucedido, o cliente recebe um ID exclusivo atribuído pelo servidor e o Menu Principal CLI fica totalmente disponível para operações comerciais.
