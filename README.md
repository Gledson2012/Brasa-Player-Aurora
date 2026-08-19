# Music Player

Player Android offline com Jetpack Compose, Media3 e Room.

Recursos incluídos: waveform visualizer com captura do áudio quando suportada pelo dispositivo, gestos no player, transição gradual entre faixas, scrobbling Last.fm, editor de letras LRC, editor de metadados/capas e backup/restauração em JSON.

## Executar localmente

Pré-requisito: [Android Studio](https://developer.android.com/studio).

1. Abra este diretório no Android Studio.
2. Aguarde a sincronização do Gradle e instale o SDK indicado pelo projeto.
3. Execute em um emulador ou dispositivo físico.

A permissão de músicas é solicitada somente quando você toca em `Temas > Biblioteca de Áudio Offline > Escanear`. A importação individual usa o seletor de arquivos do Android e preserva o acesso à URI escolhida.

No menu de uma faixa, use `Editar metadados` para corrigir título, artista, álbum, gênero ou selecionar uma capa personalizada. A alteração atualiza a biblioteca local; o arquivo de áudio original não é regravado.

O player também pode ser controlado por notificações, tela de bloqueio e Android Auto. O Android Auto acompanha as alterações da biblioteca enquanto o serviço de reprodução está ativo.

Arquivos encontrados pelo MediaStore que deixarem de existir são mantidos na biblioteca, mas marcados como indisponíveis. Arquivos importados pelo seletor do Android preservam a permissão persistente da URI e não são removidos por um novo escaneamento.

## Last.fm

Abra `Temas > Configurar Last.fm`, crie uma aplicação em `last.fm/api/account/create`, informe a API key e o API secret, autorize a conta no navegador e ative o envio de scrobbles.

## Backup

Em `Temas > Sincronização e dados`, exporte um arquivo JSON. A restauração substitui os dados atuais e pede confirmação. Antes da substituição, o aplicativo cria até três cópias locais de segurança em seu armazenamento privado. O backup guarda metadados e referências às músicas locais; os arquivos de áudio e as credenciais do Last.fm não são copiados. O backup automático do Android exclui a base e as preferências do Last.fm para evitar restaurar URIs obsoletas ou credenciais.

## Testes

Com um JDK configurado, execute `./gradlew test` e, para a base Room, `./gradlew connectedAndroidTest`. Os testes locais cobrem parsing de letras; os instrumentados cobrem a identidade de arquivos e a inserção transacional em playlists.
