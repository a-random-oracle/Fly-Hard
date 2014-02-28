Mission Control
========

## Notices
* __Richard will be merging fullscreen back into master tonight. Please note that this will involve changes to many classes. The full list of classes which _may_ be modified is here: [_classes which may be modified_](https://github.com/MWUK/Fly-Hard/compare/fullscreen#files_bucket)__

* __Please use issue numbers when committing changes/fixes relating to defined issues, it makes everything more trackable__ (also you don't have to be as descriptive, i.e. `Fixed #n` would be easier and as descriptive than `I fixed the issue where everything explodes if you click the middle mouse button`)
* [Tim Time happened](https://github.com/MWUK/Fly-Hard/blob/master/Docs/Tim%20Time/28-2-14.md).
* Super SEPR Saturday Shenanigans __2014/03/01 14:30UTC__ 
* Please declare what classes you're working on, for the benefit of others who might be working concurrently, so as to avoid conflicts.
 * Preferably via the table at the bottom.
 * Or by messaging [Mark](http://github.com/MWUK) who'll update the table on your behalf.
* Any issues with the Tasks table, change (if it is independent of others), or raise on Facebook/with dependent individual directly.
* [Mark](https://github.com/MWUK) has started documenting all the [variables](https://github.com/MWUK/Fly-Hard/wiki/Variables) (inc. name changes amongst other info) because he really loathes fun and has developed sudden OCD.

## To Do

| Item | Remarks | Due |
|:-----|:--------|----:|
| Fix variable names. ✔ | [Issue](https://github.com/mwuk/fly-hard/issues/2) | __Complete__ |
| Add in a second airport. | [Milestone](https://github.com/MWUK/Fly-Hard/issues?milestone=2&page=1&sort=created&state=open) | Mid-March |
| Make the game multiplayer-enabled. | [Milestone](https://github.com/MWUK/Fly-Hard/issues?direction=asc&milestone=3&page=1&sort=created&state=open)| Mid-April |
| Add the ability to transfer control of a plane. | [Issue](https://github.com/MWUK/Fly-Hard/issues/5) | Late-April |
| Make the thing resize. |  | _Whenever_ |
| Replace `rectangle.java` | | Tomorrow |

### Individual Tasks, Roles, Responsibilities, and other information of note.

_These are not final/rigid. Any issues, update/query as you wish :D_

| Member | Tasks | File/Source |
|:-------|:-----|:------------|
| [Wanderlust](http://github.com/a-random-oracle) | Full-scream | [_fullscreen branch_](https://github.com/MWUK/Fly-Hard/tree/fullscreen) |
| [200% Richard](http://github.com/RMCKirby) | Networking | [`graphics.java`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/lib/jog/graphics.java), [`scn*`](https://github.com/MWUK/Fly-Hard/tree/master/BTC/src/scn), and [_networking branch_](https://github.com/MWUK/Fly-Hard/tree/networking) |
| [Bonus Mark](http://github.com/MWUK) | Wandering Planes (issue to follow) |  [`Aircraft.java`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/scn/Aircraft.java) |
| [50% Jaron](http://github.com/JaronAli) | [Search and Rescue](https://github.com/MWUK/FlyHard/issues/10) | [`Aircraft.java`](https://github.com/MWUK/Fly-Hard/blob/master/BTC/src/scn/Aircraft.java) |
| [I Can't Believe It's Not Emily](http://github.com/Emily-Hall) | Idle / Scenarios | _None_ |
| [Such Hopkins](http://github.com/Salvner) | Idle / Scenarios | _None_ |
| [git --Jon](http://github.com/Lixquid) |  | _None_ |

## Notes

* `.classpath`s might be a little weird. Check yourself before you wreck yourself. 
 * A copy should be on Facebook - just make sure you call the project 'Fly Hard'.
