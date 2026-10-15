# Requisitos Funcionais e Não Funcionais

## Requisitos funcionais

| Código | Requisito | Onde está |
|--------|-----------|-----------|
| RF01 | O sistema deve autenticar operadores por e-mail e senha e só liberar o painel a usuários autenticados. | `AuthController`, `Login.vue`, guard do router |
| RF02 | O sistema deve ter dois perfis: Administrador (gerencia usuários) e Operador. | `UsuarioController` (`ROLE_ADMIN`) |
| RF03 | O Dashboard deve exibir total de torres, ligadas, desligadas, em manutenção, com alerta, bateria crítica, consumo estimado e tempo total de atividade. | `DashboardService`, `Dashboard.vue` |
| RF04 | O Dashboard deve exibir gráficos de consumo, distribuição por status, alertas por nível e disponibilidade. | `Dashboard.vue` |
| RF05 | O sistema deve cadastrar, consultar, editar e excluir torres. | `TorreController`, `Torres.vue` |
| RF06 | O sistema deve pesquisar torres por nome ou código e filtrar por status e por setor. | `TorreRepository.pesquisar` |
| RF07 | O sistema deve ter página de monitoramento com indicadores Normal, Alerta, Crítico e Manutenção, mostrando estado, bateria, combustível, localização, modo e última comunicação. | `Monitoramento.vue` |
| RF08 | O sistema deve gerar alerta automático quando bateria ou combustível ficar abaixo dos limites (atenção e crítico) sem duplicar alertas ativos. | `AlertaService.verificarNiveis` |
| RF09 | O operador deve poder ligar, desligar e escolher os modos econômico, normal, intensidade máxima e cerração, com confirmação prévia. | `TorreService`, `TorrePainel.vue` |
| RF10 | O operador deve poder aplicar um comando a várias torres de uma vez; torres em manutenção são ignoradas e informadas. | `TorreService.executarComandoGrupo` |
| RF11 | O sistema deve gerenciar manutenções preventivas e corretivas (abrir, iniciar, concluir, cancelar). | `ManutencaoService`, `Manutencoes.vue` |
| RF12 | Ao iniciar uma manutenção a torre passa para "Em manutenção"; ao concluir, pode voltar à operação. | `ManutencaoService` |
| RF13 | O sistema deve registrar e listar alertas (bateria, combustível, falha técnica, sem comunicação, manutenção pendente, consumo elevado), com nível Informativo, Atenção ou Crítico, e permitir marcá-los como resolvidos. | `AlertaService`, `Alertas.vue` |
| RF14 | O sistema deve registrar no histórico data, hora, torre, operação e usuário responsável de toda alteração importante. | `HistoricoService` |
| RF15 | O backend deve receber telemetria de dispositivos (luminosidade, bateria, estado), validar a torre, registrar a leitura e atualizar a torre. | `TelemetriaService` |
| RF16 | O dispositivo deve poder consultar comandos pendentes por polling; cada comando é entregue uma única vez. | `ComandoPendenteService` |
| RF17 | O sistema deve alertar a ausência de comunicação de torres conectadas a dispositivo e resolver o alerta quando a comunicação voltar. | `VerificadorComunicacao` |
| RF18 | O sistema deve registrar leituras de sensores e apagar as mais antigas que o período de retenção. | `LeituraSensor`, `VerificadorComunicacao` |

## Requisitos não funcionais

| Código | Requisito |
|--------|-----------|
| RNF01 | Backend em Java 17 com Spring Boot, Spring Web, Spring Data JPA e Hibernate; API REST. |
| RNF02 | Frontend em Vue 3 com Vue Router e Axios; banco MySQL 8. |
| RNF03 | Arquitetura em camadas no backend (controller, service, repository, model, dto, config, exception); regras de negócio apenas nos services. |
| RNF04 | O sistema deve iniciar com `docker compose up --build`, com dados do MySQL em volume persistente e configurações sensíveis por variáveis de ambiente. |
| RNF05 | As senhas devem ser armazenadas com hash BCrypt; a API é stateless com JWT (expiração de 8 h). |
| RNF06 | Os erros devem seguir o formato padrão `{status, erro, mensagem, timestamp}`, sem stack trace nem detalhes internos. |
| RNF07 | Os códigos HTTP devem refletir a situação: 200, 201, 204, 400, 401, 403, 404, 500. |
| RNF08 | A interface deve ser responsiva (desktop, notebook, tablet e smartphone), legível e rápida, priorizando a identificação de problemas. |
| RNF09 | O módulo IoT deve ser isolado: o sistema funciona normalmente sem dispositivos, e os dispositivos se autenticam por chave de API (`X-API-KEY`). |
| RNF10 | Os dados do painel de monitoramento devem se atualizar sozinhos (10 s no monitoramento, 15 s no dashboard). |
| RNF11 | O código deve ser simples e organizado para manutenção por uma equipe de estudantes, com testes automatizados das regras principais e do fluxo da API. |
