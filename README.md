# 🖥️ Data Center — Sistema de Monitoramento e Controle Distribuído

Sistema de simulação de um Data Center desenvolvido em **Java 21**, com foco em comunicação entre processos, monitoramento de equipamentos, descoberta de serviços e controle remoto de dispositivos.

O projeto simula a comunicação entre diferentes componentes de uma infraestrutura de Data Center, permitindo monitorar sensores, identificar equipamentos disponíveis e controlar sistemas remotamente.

## 🎯 Objetivo

Desenvolver uma aplicação distribuída capaz de simular o monitoramento e o gerenciamento de equipamentos de um Data Center, explorando conceitos fundamentais de sistemas distribuídos, comunicação em rede e tolerância a falhas.

## ⚙️ Arquitetura do sistema

A aplicação é organizada em componentes independentes que se comunicam por diferentes protocolos de rede.

- **Gateway:** centraliza a comunicação com os dispositivos.
- **Temperature Sensor:** simula a coleta e o envio de dados de temperatura.
- **Power Sensor:** simula o monitoramento do consumo de energia.
- **Cooling System:** representa o sistema de refrigeração, permitindo consultar seu estado e alterar parâmetros remotamente.

## 🚀 Funcionalidades

- Monitoramento de temperatura por UDP.
- Monitoramento de informações de energia.
- Descoberta automática de dispositivos utilizando multicast.
- Comunicação entre componentes por gRPC.
- Controle remoto do sistema de refrigeração.
- Consulta do estado dos equipamentos.
- Registro de informações em arquivos CSV.
- Simulação de detecção de falhas e indisponibilidade de componentes.

## 🛠️ Tecnologias utilizadas

| Tecnologia | Aplicação |
|---|---|
| Java 21 | Desenvolvimento da aplicação |
| Maven | Gerenciamento de dependências e build |
| UDP | Comunicação entre sensores e gateway |
| Multicast | Descoberta de dispositivos |
| gRPC | Comunicação e controle remoto |
| Protocol Buffers | Definição e serialização de mensagens |
| CSV | Persistência de dados |

## 💻 Como executar

### Pré-requisitos

- JDK 21 ou superior.
- Apache Maven.
- Git.

### 1. Clone o repositório

```bash
git clone https://github.com/IgutoI/projeto-distribuido.git
```

### 2. Entre na pasta

```bash
cd projeto-distribuido
```

### 3. Compile o projeto

```bash
mvn clean compile
```

### 4. Execute os componentes

Execute as classes principais dos componentes conforme a arquitetura do projeto.

Os componentes que se comunicam pela rede precisam estar ativos para que a simulação funcione corretamente.

## 📚 Conceitos estudados

- Sistemas distribuídos.
- Comunicação entre processos (IPC).
- Protocolos de comunicação em rede.
- Arquitetura cliente-servidor.
- Descoberta de serviços.
- Serialização de dados.
- Monitoramento e controle remoto.
- Gerenciamento de dependências com Maven.

## 🎓 Contexto acadêmico

Projeto desenvolvido como atividade acadêmica da disciplina **Distribuição de Processos e Dados**, no curso de Engenharia da Computação.

## 👨‍💻 Autor

**igutoi**

- GitHub: [@IgutoI](https://github.com/IgutoI)
- Repositório: [projeto-distribuido](https://github.com/IgutoI/projeto-distribuido)

---

*Projeto acadêmico desenvolvido para explorar tecnologias e conceitos de sistemas distribuídos.*
