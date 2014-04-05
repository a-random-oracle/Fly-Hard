Mission Control - Server Branch
===============================

## Notices
* People will need to get themselves set up with Openshift accounts if they want access to the server [Openshift Sign Up](https://www.openshift.com/app/account/new). Once you have an account, text/message me and I'll add you to the list of collaborators on the server.

## To Do

| Item | Remarks | Affects | Due |
|:-----|:--------|:--------|----:|
| Get startup times synchronised | Currently when one player starts multiplayer before the other, they will just enter the game early. It would be better to have them wait until all players are ready (and then start all players at once). | Server | This weekend |
| Get game exits synchronised | The game should stop for both players when either one exits early. | Server and clients | This weekend |
| Get different screen sizes working | At present, waypoints etc. appear in a subsection of the screen when viewed from a larger monitor. | Clients | Easter weekend |
| Add a game lobby | It would be nice if players could select which opponent they wish to play. | Server and clients | Some point in the near future |

### Individual Tasks, Roles, Responsibilities, and other information of note.

All links given below are with respect to the [__server__](https://github.com/mwuk/Fly-Hard/tree/server) branch

| Member | Tasks | File/Source |
|:-------|:------|:------------|
| [Richard A](http://github.com/a-random-oracle) | The server. Some client-side networking. | [__srv__](http://tomcat-teamgoa.rhcloud.com) (sever-side) and [__net__](https://github.com/mwuk/Fly-Hard/tree/server/BTC/src/net) (client-side) |
| [Richard K](http://github.com/RMCKirby) | Things | Currently just [__net__](https://github.com/mwuk/Fly-Hard/tree/server/BTC/src/net) |