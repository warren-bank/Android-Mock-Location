# Mock my GPS

An Android app that feeds a chosen position to the device's location providers in place of the real one, either as a single spot or as a simulated trip between two spots.

## Language

**Location point**:
A latitude/longitude pair the app can mock, bookmark or use as a trip end.
_Avoid_: Coordinate, geo point, position (when meaning the value itself)

**Fixed position**:
A single location point mocked continuously until stopped.
_Avoid_: Static location, pinned location

**Trip origin** / **Trip destination**:
The two location points a trip simulation moves between.
_Avoid_: Start/end, from/to

**Bookmark**:
A named location point saved for reuse.
_Avoid_: Favourite, saved place

**Purpose**:
The role an incoming location point is given: fixed position, trip origin, trip destination or new bookmark.
_Avoid_: Mode, target, type

## Getting points in

**Handoff**:
Bringing a location point into the app from outside it (another app, a link, the clipboard) rather than typing it.
_Avoid_: Import, integration

**Geo-intent**:
A handoff in which another app or link asks Android to view a location, and the app is offered as the viewer.
_Avoid_: Geo link, map intent

**Share**:
A handoff in which another app sends text through Android's share sheet, and the app reads a location point out of it.
_Avoid_: Send, share intent (when meaning the user action)

**Short link**:
A shortened map URL (such as Google Maps' `maps.app.goo.gl`) that names a location only through a redirect.
_Avoid_: Shortened URL, goo.gl link

**Link lookup**:
Asking a short link's own host where the link points, so that a location point can be read from the answer without opening the place it points to.
_Avoid_: Resolution, unshortening, expanding
