# How to make SVGs which are compatible with SvgDrawable

I wrote the SvgDrawable class to have an easy solution for drawing SVG objects to a canvas in an efficient way.

The common way to do this would be importing every object as separate SVG file in Android Studio, so that you have all objects stored as Android Vector Drawables which can then be rendered to canvas.  
The big problem is: once we've got the VectorDrawable in code, it is cached as Bitmap. Bitmaps are much more inefficient to draw and require a lot of CPU calculation when hardware acceleration is not available.

That's why I've created my own solution to keep the original SVG objects which can then be drawn with the native canvas methods like Canvas#drawCircle.  
The big advantage here is that we don't have to import every single object into Android Studio. We just have to save the (improved, read below) SVG file to the raw resource folder and here we go!

I use Inkscape when I am working with vector graphics. Here I explain the requirements for the SVG to work with SvgDrawable.

# SVG requirements

- Required viewBox size: 1080 x 1920 (measured in pixels, portrait mode), width and height attributes are not required (because they should have exactly the same size)
- Every object which should be drawn has to have an unique object ID (for later modifications). Inkscape does this automatically.
- Solid background color: rect in the background with the dimensions of the SVG (viewBox)
- SVG has to be cleaned with SVGOMG (continue reading for more information)

## Unifying with SVGOMG

The SVG should be unified with the free online tool SVGOMG, which you can find [here](https://jakearchibald.github.io/svgomg/).
There you have to **turn on all options** and then **turn the following off**:

- Show original
- Remove xmlns
- Clean IDs
- Remove raster images
- Shapes to (smaller) paths
- Move attrs to parent group
- Move group attrs to elements
- Merge paths
- Replace duplicate elements with links

The precision slider should be on 3 or 2, make sure that the preview looks good!

:warning: **Path objects:** in some cases paths are broken when you saved the file as "Plain SVG" in Inkscape. When you save the file as Inkscape SVG it should work.

## Supported objects

Make sure that you keep the original types (e.g. circle, rect). Do not convert them to paths, they are much more inefficient to draw!

- ´g´ (Group)
- ´path´
- ´rect´ (even with rounded corners)
- ´circle´
- ´ellipse´
- ´image´

## Groups (g)

- child groups in groups are not supported

## Transformation

Following transformation types are supported:

- Rotation
- Translation

Transformation matrix and scaling are not supported.

## SvgDrawable#findObjectById(String id)

- only returns objects which are not in a group
- therefore elevation can only be set on non-child objects and groups