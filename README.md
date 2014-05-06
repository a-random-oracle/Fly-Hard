Mission Control
========

## FINAL APPROACH

## GUI (for Mark/Sam('s Manual))

| Scene/File         | Completion | ToDo     | Assignee |
| :---------         | :--------- | :------  | :------- |
| `Title`            | __100%__   | _Nothing_| _Nobody_   |
| `DifficultySelect` | _100%*_    | _Nothing_| _Nobody_   |
| `Lobby`            | 85%        | Add yellow title | @mwuk |
| `Credits`          | 95%        | Change game title/music? | @mwuk |
| `SinglePlayerGame` | __99%__    | __Fix explosions__    | @RMCKirby |
| `MultiPlayerGame`  | __100%__   | _Nothing_| _Nobody_ |
| `Game`             | __100%__   | _Nothing_| _Nobody_ |
| `GameOver`         | 90%        | __Fix explosions__ | @RMCKirby |
| `GameOverMulti`    | __100%__        | _Nothing_ | _Nobody_ |

## DANGER ZONE

* Player lives (in MP) are currently (potentially) set to 3000. @a-random-oracle needs to change this.

----
# Archive

## Notices
* Airspaces will adopt a zoning system a la [ice hockey](http://en.wikipedia.org/wiki/Ice_hockey_rink#Zones).
* Planes can crash. I think? I don't know what happened or what year this is.
* Points mean prizes. (Need to consider unit. Perhaps Jeremies?)
* [Tim Time happened](https://github.com/MWUK/Fly-Hard/blob/master/Docs/Tim%20Time/28-2-14.md).
* Super SEPR Saturday Shenanigans _also happened._
* Please declare what classes you're working on, for the benefit of others who might be working concurrently, so as to avoid conflicts.
 * Preferably via the table at the bottom.
 * Or by messaging [Mark](http://github.com/MWUK) who'll update the table on your behalf.
* Any issues with the Tasks table, change (if it is independent of others), or raise on Facebook/with dependent individual directly.
* [Mark](https://github.com/MWUK) has started documenting all the [variables](https://github.com/MWUK/Fly-Hard/wiki/Variables) (inc. name changes amongst other info) because he really loathes fun and has developed sudden OCD.
* The repo will be deleted/cleared periodically.
* __Test as you go on!__ Testing should be implemented/considered as code is implemented.
* __Please use issue numbers when committing changes/fixes relating to defined issues, it makes everything more trackable__ (also you don't have to be as descriptive, i.e. `Fixed #n` would be easier and as descriptive than `I fixed the issue where everything explodes if you click the middle mouse button`)
* People should keep ideas for powerups. For example a power-up where all planes turn into Jeremies.

## To Domark

| Item | Remarks | Due |
|:-----|:--------|----:|
| Fix variable names. ✔ | [Issue](https://github.com/mwuk/fly-hard/issues/2) | __Complete__ |
| Add in a second airport. ✔ | [Milestone](https://github.com/MWUK/Fly-Hard/issues?milestone=2&page=1&sort=created&state=open) | __Complete__ |
| Make the game multiplayer-enabled. | [Milestone](https://github.com/MWUK/Fly-Hard/issues?direction=asc&milestone=3&page=1&sort=created&state=open)| Mid-April |
| Add the ability to transfer control of a plane. | [Issue](https://github.com/MWUK/Fly-Hard/issues/5) | Late-April |
| Make the thing resize. ✔ |  | __Complete__ |
| Flightstrips | | Mid-April |
| Replace `rectangle.java` | | Tomorrow |

### Individual Tasks, Roles, Responsibilities, and other information of note.

_These are not final/rigid. Any issues, update/query as you wish :D_

| Member | Tasks | File/Source |
|:-------|:-----|:------------|
| [Wanderlust](http://github.com/a-random-oracle) | Multiplayer. Some networking stuff. | [`Player.java`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/cls/Player.java), [`Game.java`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/scn/Game.java), [`SinglePlayerGame.java`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/scn/SinglePlayerGame.java), [`MultiPlayerGame.java`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/scn/MultiPlayerGame.java) and [`net*`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/net) (and anything else which needs editing for multiplayer) |
| [200% Richard](http://github.com/RMCKirby) | Networking | [`graphics.java`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/lib/jog/graphics.java), [`scn*`](https://github.com/MWUK/Fly-Hard/tree/master/BTC/src/scn), and [_networking branch_](https://github.com/MWUK/Fly-Hard/tree/networking) |
| [Bonus Mark](http://github.com/MWUK) | __S__uggestive __T__raffic __R__eporting __I__nteractive __P__resentation & __S__hoehorns |  [Saab](http://www.saabgroup.com/Global/Documents%20and%20Images/Civil%20Security/Air%20Transportation%20and%20Airport%20Security/e-Strip/E-Strip-WEB.pdf) _mostly…_ Small doses of [`iOS-7`](https://github.com/MWUK/Fly-Hard/tree/iOS-7). |
| [50% Jaron](http://github.com/JaronAli) | Powerdowns/Score | _Snapchat_ |
| [I Can't Believe It's Not Emily](http://github.com/Emily-Hall) | Powerups/Score | _Potential New Class_ |
| [Such Hopkins](http://github.com/Salvner) | Feigning death | _None_ |
| [git --Jon](http://github.com/Lixquid) | Website / Gazing Wistfully at MWUK's GUI overhaul | [L'Internet](http://goa.lixquid.co.uk) |

## Notes

* `.classpath`s might be a little weird. Check yourself before you wreck yourself.
 * A copy should be on Facebook - just make sure you call the project 'Fly Hard'.
