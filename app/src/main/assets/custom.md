# SVG requirements

- required viewBox size: 1080 x 1920 (measured in pixels, portrait mode), width and height attributes are not required (because they should have exactly the same size)
- Every object which should be drawn has to have an unique object ID (for later modifications)
- Background color: rect in the background with the dimensions of the SVG

SVGOMG: https://jakearchibald.github.io/svgomg/


## Supported objects

- ´g´
- ´path´
- ´rect´ (+rounded)
- ´circle´
- ´ellipse´
- ´image´

## Groups (g)

- child groups in groups are not supported
- group transformation is not supported, rotation needs to be applied to every child separately