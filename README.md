Mission Control - Server Branch
===============================

## Notices
* People will need to get themselves set up with Openshift accounts if they want access to the server ([Openshift Sign Up](https://www.openshift.com/app/account/new)). Once you have an account, text/message me and I'll add you to the list of collaborators on the server.

## To Do
| Item | Remarks | Affects | Due |
|:-----|:--------|:--------|----:|
| Get startup times synchronised ✔ | Currently when one player starts multiplayer before the other, they will just enter the game early. It would be better to have them wait until all players are ready (and then start all players at once). | Server | __Complete__ |
| Get game exits synchronised ✔ | The game should stop for both players when either one exits early. | Server and clients | __Complete__ |
| Get different screen sizes working ✔  | At present, waypoints etc. appear in a subsection of the screen when viewed from a larger monitor. | Clients | __Complete__ |
| Add a game lobby ✔  | It would be nice if players could select which opponent they wish to play. | Server and clients | __Complete__ |

### Individual Tasks, Roles, Responsibilities, and other information of note.

All links given below are with respect to the [__server branch__](https://github.com/mwuk/Fly-Hard/tree/server)

| Member | Tasks | File/Source |
|:-------|:------|:------------|
| [Richard A](http://github.com/a-random-oracle) | The server. Some client-side networking. | [`srv.*`](http://tomcat-teamgoa.rhcloud.com) (sever-side) and [`net.*`](https://github.com/mwuk/Fly-Hard/tree/server/BTC/src/net) (client-side). Also some [`Lobby.java`](https://github.com/mwuk/Fly-Hard/tree/server/BTC/src/scn/Lobby.java) stuff. |
| [Richard K](http://github.com/RMCKirby) | The Lobby | [`Lobby.java`](https://github.com/mwuk/Fly-Hard/tree/server/BTC/src/scn/Lobby.java) |
