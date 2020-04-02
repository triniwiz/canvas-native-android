package com.github.triniwiz.canvasdemo

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.triniwiz.canvas.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos


class MainActivity : AppCompatActivity() {
    var canvas: CanvasView? = null
    var svg: SVGView? = null
    var ctx: CanvasRenderingContext2D? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        canvas = findViewById(R.id.canvasView)
        init()
    }

    var r = 100f; // Radius
    var p0 = KeyValue(0f, 50f);

    var p1 = KeyValue(100f, 100f)
    var p2 = KeyValue(150f, 50f);
    var p3 = KeyValue(200f, 100f);

    private fun textPoint(
        ctx: CanvasRenderingContext2D,
        p: KeyValue,
        offset: KeyValue,
        i: Int = 0
    ) {
        val x = offset.x
        val y = offset.y
        ctx.beginPath()
        ctx.arc(p.x, p.y, 2f, 0f, (Math.PI * 2).toFloat())
        ctx.fill()
        ctx.fillText("" + i + ":" + p.x + "," + p.y, p.x + x, p.y + y)
    }

    fun drawPoints(ctx: CanvasRenderingContext2D, points: Array<KeyValue>) {
        for (point in points) {
            val i = points.indexOf(point)
            val p = points[i]
            textPoint(ctx, p, KeyValue(0, -20), i)
        }
    }

    fun drawCreateImageData(ctx: CanvasRenderingContext2D) {
        val imageData = ctx.createImageData((100 * scale).toInt(), (100 * scale).toInt())
// Iterate through every pixel
        var i = 0
        val size = imageData.data.size - 1
        while (i <= size) {
            var next = 0
            if (i + 0 <= size) {
                imageData.data[i + 0] = 190.toByte()
                next = 1
            }
            if (i + 1 <= size) {
                imageData.data[i + 1] = 0.toByte()
                next = 2
            }
            if (i + 2 <= size) {
                imageData.data[i + 2] = 210.toByte()
                next = 3
            }
            if (i + 3 <= size) {
                imageData.data[i + 3] = 255.toByte()
                next = 4
            }
            i += next
        }
        ctx.putImageData(imageData)
    }

    fun drawArc(ctx: CanvasRenderingContext2D, points: Array<KeyValue>, r: Float) {
        val p0 = points[0]
        val p1 = points[1]
        val p2 = points[2]
        ctx.beginPath();
        ctx.moveTo(p0.x, p0.y);
        ctx.arcTo(p1.x, p1.y, p2.x, p2.y, r);
        ctx.lineTo(p2.x, p2.y);
        ctx.stroke();
    }

    var t0 = 0.0
    var rr = 0.0 // the radius that changes over time
    var a = 0.0 // angle
    private val PI2 = Math.PI * 2
    private var lastTime = 0L
    var timeToCall = 0L
    var handler = Handler(Looper.getMainLooper())


    fun solarAnimation(ctx: CanvasRenderingContext2D) {
        AnimationFrame.requestAnimationFrame { called ->
            run {
                animateSolarSystem(ctx, called)
            }
        }
    }

    companion object {
        @JvmStatic
        fun init() {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
    }

    var CanvasXSize = 800;
    var CanvasYSize = 200;
    var speed = 30; // lower is faster
    var scale = 1.05;
    var y = -4.5; // vertical offset

// Main program

    var dx = 0.75;
    var imgW = 0
    var imgH = 0
    var x = 0;
    var clearX = 0
    var clearY = 0

    fun drawPano(ctx: CanvasRenderingContext2D, img: Bitmap) {
        val runnableCode = object : Runnable {
            override fun run() {
                ctx.clearRect(0F, 0F, clearX.toFloat(), clearY.toFloat()); // clear the canvas

                // if image is <= Canvas Size
                if (imgW <= CanvasXSize) {
                    // reset, start from beginning
                    if (x > CanvasXSize) {
                        x += -imgW;
                    }
                    // draw additional image1
                    if (x > 0) {
                        ctx.drawImage(
                            img,
                            (-imgW + x).toFloat(),
                            y.toFloat(),
                            imgW.toFloat(),
                            imgH.toFloat()
                        );
                    }
                    // draw additional image2
                    if (x - imgW > 0) {
                        ctx.drawImage(
                            img,
                            (-imgW * 2 + x).toFloat(),
                            y.toFloat(),
                            imgW.toFloat(),
                            imgH.toFloat()
                        );
                    }
                }

                // image is > Canvas Size
                else {
                    // reset, start from beginning
                    if (x > (CanvasXSize)) {
                        x = CanvasXSize - imgW;
                    }
                    // draw aditional image
                    if (x > (CanvasXSize - imgW)) {
                        ctx.drawImage(
                            img,
                            (x - imgW + 1).toFloat(),
                            y.toFloat(),
                            imgW.toFloat(),
                            imgH.toFloat()
                        );
                    }
                }
                // draw image
                ctx.drawImage(img, x.toFloat(), y.toFloat(), imgW.toFloat(), imgH.toFloat());
                // amount to move
                x += (dx).toInt()
                handler.postDelayed(this, speed.toLong())
            }
        }
        handler.post(runnableCode)
    }

    fun panoramaSectionAnimation(ctx: CanvasRenderingContext2D) {
        CanvasXSize = ctx.canvas.width
        CanvasYSize = ctx.canvas.height
        Log.d("com.github", "w " + CanvasXSize + " H " + CanvasYSize)
        try {
            val file = File(filesDir, "Capitan_Meadows,_Yosemite_National_Park.jpg")
            var img: Bitmap?
            if (file.exists()) {
                img = BitmapFactory.decodeFile(file.absolutePath)
                Log.d("com.github", "w " + img?.width + " H " + img?.height)
            } else {
                val url =
                    URL("https://mdn.mozillademos.org/files/4553/Capitan_Meadows,_Yosemite_National_Park.jpg")
                val fs = FileOutputStream(file)
                url.openStream().use { input ->
                    fs.use { output ->
                        input.copyTo(output)
                    }
                }
                img = BitmapFactory.decodeFile(file.absolutePath)
            }

            imgW = (img!!.width * scale).toInt()

            imgH = (img.height * scale).toInt()

            if (imgW > CanvasXSize) {
                // image larger than canvas
                x = CanvasXSize - imgW;
            }
            if (imgW > CanvasXSize) {
                // image width larger than canvas
                clearX = imgW;
            } else {
                clearX = CanvasXSize;
            }
            if (imgH > CanvasYSize) {
                // image height larger than canvas
                clearY = imgH;
            } else {
                clearY = CanvasYSize;
            }

            drawPano(ctx, img)

        } catch (e: IOException) {

        }
    }

    var sun: Bitmap? = null
    var moon: Bitmap? = null
    var earth: Bitmap? = null

    fun animateSolarSystem(ctx: CanvasRenderingContext2D, t: Long) {
        try {
            val sunFile = File(filesDir, "Canvas_sun.png")
            val moonFile = File(filesDir, "Canvas_moon.png")
            val earthFile = File(filesDir, "Canvas_earth.png")


            if (sun == null) {
                if (sunFile.exists()) {
                    sun = BitmapFactory.decodeFile(sunFile.absolutePath)
                } else {
                    val url = URL("https://mdn.mozillademos.org/files/1456/Canvas_sun.png")
                    val fs = FileOutputStream(sunFile)
                    url.openStream().use { input ->
                        fs.use { output ->
                            input.copyTo(output)
                        }
                    }
                    sun = BitmapFactory.decodeFile(sunFile.absolutePath)
                }
            }


            if (moon == null) {
                if (moonFile.exists()) {
                    moon = BitmapFactory.decodeFile(moonFile.absolutePath)
                } else {
                    val url = URL("https://mdn.mozillademos.org/files/1443/Canvas_moon.png")
                    val fs = FileOutputStream(moonFile)
                    url.openStream().use { input ->
                        fs.use { output ->
                            input.copyTo(output)
                        }
                    }
                    moon = BitmapFactory.decodeFile(moonFile.absolutePath)
                }
            }

            if (earth == null) {
                if (earthFile.exists()) {
                    earth = BitmapFactory.decodeFile(earthFile.absolutePath)
                } else {
                    val url = URL("https://mdn.mozillademos.org/files/1429/Canvas_earth.png")
                    val fs = FileOutputStream(earthFile)
                    url.openStream().use { input ->
                        fs.use { output ->
                            input.copyTo(output)
                        }
                    }
                    earth = BitmapFactory.decodeFile(earthFile.absolutePath)
                }
            }



            ctx.globalCompositeOperation = CanvasCompositeOperationType.DestinationOver
            ctx.clearRect(0F, 0F, 300F, 300F) // clear canvas

            ctx.fillStyle = CanvasColorStyle.Color("rgba(0, 0, 0, 0.4)")
            ctx.strokeStyle = CanvasColorStyle.Color("rgba(0, 153, 255, 0.4)")
            ctx.save()
            ctx.translate(150F, 150F)

            // Earth
            val time = Date()
            ctx.rotate((2 * Math.PI / 60 * (time.time / 1000) + 2 * Math.PI / 60000 * time.time).toFloat())
            ctx.translate(105F, 0F)
            ctx.fillRect(0F, -12F, 40F, 24F) // Shadow
            ctx.drawImage(earth, -12F, -12F)

            // Moon
            ctx.save()
            ctx.rotate((2 * Math.PI / 6 * (time.time / 1000) + 2 * Math.PI / 6000 * time.time).toFloat())
            ctx.translate(0F, 28.5F)
            ctx.drawImage(moon, -3.5F, -3.5F)
            ctx.restore()

            ctx.restore()

            ctx.beginPath()
            ctx.arc(150F, 150F, 105F, 0F, (PI * 2).toFloat(), false) // Earth orbit
            ctx.stroke()

            ctx.drawImage(sun, 0F, 0F, 300F, 300F)

            AnimationFrame.requestAnimationFrame { called ->
                run {
                    animateSolarSystem(ctx, called)
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun loop(ctx: CanvasRenderingContext2D, t: Long) {
        t0 = t / 1000.0
        a = t0 % PI2
        rr = abs(cos(a) * r)
        ctx.clearRect(0f, 0f, ctx.canvas.width.toFloat(), ctx.canvas.height.toFloat());
        val points = arrayOf(p1, p2, p3)
        drawArc(ctx, points, rr.toFloat())
        drawPoints(ctx, points)

        AnimationFrame.requestAnimationFrame { called ->
            loop(ctx, called)
        }
    }

    fun getImageData(ctx: CanvasRenderingContext2D) {
        ctx.rect(10F, 10F, 100F, 100F)
        ctx.fill()

        val imageData = ctx.getImageData(60F, 60F, 200, 100)
        ctx.putImageData(imageData, 150F, 10F)
    }

    @SuppressLint("NewApi")
    fun drawShadowAlpha(ctx: CanvasRenderingContext2D) {
        // Shadow

        ctx.shadowColor = Color.argb(0.8F, (255 / 255).toFloat(), 0F, 0F)
        ctx.shadowBlur = 8F
        ctx.shadowOffsetX = 30F
        ctx.shadowOffsetY = 20F

// Filled rectangle
        ctx.fillStyle = CanvasColorStyle.Color(Color.argb(0.2F, 0F, (255 / 255).toFloat(), 0F))
        ctx.fillRect(10F, 10F, 150F, 100F)

// Stroked rectangle
        ctx.lineWidth = 10F
        ctx.strokeStyle = CanvasColorStyle.Color(Color.argb(0.6F, 0F, 0F, (255 / 255).toFloat()))
        ctx.strokeRect(10F, 10F, 150F, 100F);
    }

    fun drawShadow(ctx: CanvasRenderingContext2D) {
        // Shadow
        ctx.shadowColor = Color.RED;
        ctx.shadowOffsetX = 10F;
        ctx.shadowOffsetY = 10F;

// Filled rectangle
        ctx.fillRect(20F, 20F, 100F, 100F);

// Stroked rectangle
        ctx.lineWidth = 6F;
        ctx.strokeRect(170F, 20F, 100F, 100F);
    }

    fun drawText(ctx: CanvasRenderingContext2D) {
        ctx.font = (50).toString() + "px serif"
        ctx.shadowColor = Color.BLUE
        ctx.shadowBlur = 10F
        ctx.shadowOffsetX = 100F
        ctx.shadowOffsetY = 100F
        ctx.strokeText("Hello world", 50F, 90F);
    }

    val circle = "<svg height=\"100\" width=\"100\">" +
            "<circle cx=\"50\" cy=\"50\" r=\"40\" stroke=\"black\" stroke-width=\"3\" fill=\"red\" />\n" +
            "  Sorry, your browser does not support inline SVG.  \n" +
            "</svg> "
    val rect = "<svg width=\"400\" height=\"110\">\n" +
            "  <rect width=\"300\" height=\"100\" style=\"fill:rgb(0,0,255);stroke-width:3;stroke:rgb(0,0,0)\" />\n" +
            "  Sorry, your browser does not support inline SVG.  \n" +
            "</svg>"


    val alphaRect = "<svg width=\"400\" height=\"180\">\n" +
            "  <rect x=\"50\" y=\"20\" width=\"150\" height=\"150\" style=\"fill:blue;stroke:pink;stroke-width:5;fill-opacity:0.1;stroke-opacity:0.9\" />\n" +
            "  Sorry, your browser does not support inline SVG.  \n" +
            "</svg>"

    var path = "<svg height=\"400\" width=\"450\">\n" +
            "  <path id=\"lineAB\" d=\"M 100 350 l 150 -300\" stroke=\"red\"\n" +
            "  stroke-width=\"3\" fill=\"none\" />\n" +
            "  <path id=\"lineBC\" d=\"M 250 50 l 150 300\" stroke=\"red\"\n" +
            "  stroke-width=\"3\" fill=\"none\" />\n" +
            "  <path d=\"M 175 200 l 150 0\" stroke=\"green\" stroke-width=\"3\"\n" +
            "  fill=\"none\" />\n" +
            "  <path d=\"M 100 350 q 150 -300 300 0\" stroke=\"blue\"\n" +
            "  stroke-width=\"5\" fill=\"none\" />\n" +
            "  <!-- Mark relevant points -->\n" +
            "  <g stroke=\"black\" stroke-width=\"3\" fill=\"black\">\n" +
            "    <circle id=\"pointA\" cx=\"100\" cy=\"350\" r=\"3\" />\n" +
            "    <circle id=\"pointB\" cx=\"250\" cy=\"50\" r=\"3\" />\n" +
            "    <circle id=\"pointC\" cx=\"400\" cy=\"350\" r=\"3\" />\n" +
            "  </g>\n" +
            "  <!-- Label the points -->\n" +
            "  <g font-size=\"30\" font-family=\"sans-serif\" fill=\"black\" stroke=\"none\"\n" +
            "  text-anchor=\"middle\">\n" +
            "    <text x=\"100\" y=\"350\" dx=\"-30\">A</text>\n" +
            "    <text x=\"250\" y=\"50\" dy=\"-10\">B</text>\n" +
            "    <text x=\"400\" y=\"350\" dx=\"30\">C</text>\n" +
            "  </g>\n" +
            "</svg>"

    fun drawSVG(view: View) {

    }

    fun faceLoop(ctx: CanvasRenderingContext2D) {
        ctx.fillStyle = CanvasColorStyle.Color("white")
        ctx.fillRect(0f, 0f, ctx.canvas.width.toFloat(), ctx.canvas.height.toFloat())
        ctx.fillStyle = CanvasColorStyle.Color("black")
        drawFace(ctx)
        AnimationFrame.requestAnimationFrame {
            faceLoop(ctx)
        }
    }

    @SuppressLint("NewApi")
    fun drawFill(view: View) {
        ctx = canvas?.getContext("2d") as CanvasRenderingContext2D?
        /* ctx?.fillStyle = CanvasColorStyle.Color(Color.BLUE)
         ctx?.clearRect(0F,0F, canvas!!.width.toFloat(), canvas!!.height.toFloat())
         ctx?.fillRect(0F,0F,200f,200f)
         postDelayed(handler, Runnable{
             ctx?.clearRect(0F,0F, canvas!!.width.toFloat(), canvas!!.height.toFloat())
             ctx?.fillStyle = CanvasColorStyle.Color(Color.BLACK)
             drawImageExample(ctx!!)
         },null,4000)*/
       // drawHouse(ctx!!)
        //drawSVG(svgView!!)
      //  drawHouse(ctx!!)
        /*canvasView.toDataURLAsync {
            Log.d("com.test", "aaaa: " + it)
        }
        canvasView.toDataURLAsync {
            Log.d("com.test", "bbbb: " + it)
        }
        canvasView.toDataURLAsync {
            Log.d("com.test", "cccc: " + it)
        }
        canvasView.toDataURLAsync {
            Log.d("com.test", "dddd: " + it)
        }*/

       // drawImageExample(ctx!!)
        // drawImageSmoothingEnabled(ctx!!)
        draw(ctx!!)

        // Create clipping path
        // Create clipping path


//solarAnimation(ctx!!)
      // draw(ctx!!)

       // canvas?.flush()

      //  loop(ctx!!,0)
        /*val data = canvas!!.toDataURL();
        Log.d("com.test", "url: " + data)

        //drawImageExample(ctx!!)
       // val data = canvas!!.toData()
        //Log.d("com.test", "stfff: " + data)
        val file = File(applicationContext.filesDir,"base64.txt")
        val fos = FileOutputStream(file)
        fos.write(data.toByteArray(StandardCharsets.UTF_8))
        fos.close()
        */

    }

    fun drawFace(ctx: CanvasRenderingContext2D) {
        ctx.beginPath();
        ctx.arc(240f, 20f, 40f, 0f, Math.PI.toFloat());
        ctx.moveTo(100f, 20f);
        ctx.arc(60f, 20f, 40f, 0f, Math.PI.toFloat());
        ctx.moveTo(215f, 80f);
        ctx.arc(150f, 80f, 65f, 0f, Math.PI.toFloat());
        ctx.closePath();
        ctx.lineWidth = 6f;
        ctx.stroke();
    }

    fun drawHouse(ctx: CanvasRenderingContext2D) {
        ctx.shadowBlur = 10.0F
        ctx.shadowColor = Color.BLUE
        ctx.shadowOffsetX = 0F
        ctx.shadowOffsetY = 0F
        ctx.lineWidth = 10f;
// Wall
        ctx.strokeRect(75f, 140f, 150f, 110f);
// Door
        ctx.fillRect(130f, 190f, 40f, 60f);

// Roof
        ctx.moveTo(50f, 140f);
        ctx.lineTo(150f, 60f);
        ctx.lineTo(250f, 140f);
        ctx.closePath();
        ctx.stroke();
    }


    class Ball {
        var x = 100.0f
        var y = 100.0f
        var vx = 5.0f
        var vy = 2.0f
        var radius = 25f
        var color = Color.BLUE
        fun draw(ctx: CanvasRenderingContext2D) {
            ctx.beginPath();
            ctx.arc(x, y, radius, 0f, (Math.PI * 2).toFloat(), true);
            ctx.closePath();
            ctx.fillStyle = CanvasColorStyle.Color(color)
            ctx.fill();
        }
    }


    fun draw(ctx: CanvasRenderingContext2D) {
        var s = resources.displayMetrics.density
        var canvas = ctx.canvas
        //ctx.fillStyle = CanvasColorStyle.Color("rgba(255,255,255,0.3)")
        var width = canvas.width
        var height = canvas.height
       // ctx.fillRect(0f, 0f, width.toFloat(), height.toFloat())
        ctx.clearRect(0f,0f, canvas.width.toFloat(), canvas.height.toFloat());
        ball.draw(ctx)
        ball.x += ball.vx;
        ball.y += ball.vy;
        ball.vy *= 0.99f
        ball.vy += 0.25f

        if ((ball.y + ball.vy) > height ||
            ball.y + ball.vy < 0
        ) {
            ball.vy = -ball.vy;
        }
        if ((ball.x + ball.vx) > (width) ||
            ball.x + ball.vx < 0
        ) {
            ball.vx = -ball.vx;
        }


        AnimationFrame.requestAnimationFrame { called ->
            draw(ctx)
        }
    }

    var ball = Ball()

    fun ballExample(ctx: CanvasRenderingContext2D) {
        AnimationFrame.requestAnimationFrame { called ->
            draw(ctx)
        }
    }


    fun drawImageSmoothingQuality(ctx: CanvasRenderingContext2D) {
        try {
            val file = File(filesDir, "Canvas_createpattern.png")
            if (file.exists()) {
                val img = BitmapFactory.decodeFile(file.absolutePath)

                ctx.imageSmoothingQuality = CanvasRenderingContext2D.ImageSmoothingQuality.Low;
                ctx.drawImage(img, 0F, 0F, 300f, 150f);
            } else {
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val url = URL("https://mdn.mozillademos.org/files/222/Canvas_createpattern.png")
                val fs = FileOutputStream(file)
                url.openStream().use { input ->
                    fs.use { output ->
                        input.copyTo(output)
                    }
                }
                val img = BitmapFactory.decodeFile(file.absolutePath)

                ctx.imageSmoothingQuality = CanvasRenderingContext2D.ImageSmoothingQuality.Low;
                ctx.drawImage(img, 0F, 0F, 300F, 150F);

            }

        } catch (e: IOException) {

        }
    }

    fun drawImageSmoothingEnabled(ctx: CanvasRenderingContext2D) {
        try {
            val file = File(filesDir, "star.jpg")
            if (file.exists()) {
                val img = BitmapFactory.decodeFile(file.absolutePath)

                val w = img.width.toFloat()
                val h = img.height.toFloat()

                ctx.fillText("Source", (w * .5).toFloat(), 20F);
                ctx.drawImage(img, 0F, 24F, w, h);

                ctx.fillText("Smoothing = TRUE", (w * 2.5).toFloat(), 20F);
                ctx.imageSmoothingEnabled = true;
                ctx.drawImage(img, w, 24F, w * 3, h * 3);

                ctx.fillText("Smoothing = FALSE", w * 5.5F, 20F);
                ctx.imageSmoothingEnabled = false;
                ctx.drawImage(img, w * 4, 24F, w * 3, h * 3);
            } else {
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val url =
                    URL("https://interactive-examples.mdn.mozilla.net/media/examples/star.png")
                val fs = FileOutputStream(file)
                url.openStream().use { input ->
                    fs.use { output ->
                        input.copyTo(output)
                    }
                }
                val img = BitmapFactory.decodeFile(file.absolutePath)

                val w = img.width.toFloat()
                val h = img.height.toFloat()

                ctx.fillText("Source", (w * .5).toFloat(), 20F);
                ctx.drawImage(img, 0F, 24F, w, h);

                ctx.fillText("Smoothing = TRUE", (w * 2.5).toFloat(), 20F);
                ctx.imageSmoothingEnabled = true;
                ctx.drawImage(img, w, 24F, w * 3, h * 3);

                ctx.fillText("Smoothing = FALSE", w * 5.5F, 20F);
                ctx.imageSmoothingEnabled = false;
                ctx.drawImage(img, w * 4, 24F, w * 3, h * 3);

            }

        } catch (e: IOException) {

        }
    }

    fun drawImageExample(ctx: CanvasRenderingContext2D) {
        try {
            val file = File(filesDir, "rhino.jpg")
            if (file.exists()) {
                val image = BitmapFactory.decodeFile(file.absolutePath)
                ctx.drawImage(image, 33F, 71F, 104F, 124F, 21F, 20F, 87F, 104F)
            } else {
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val url =
                    URL("https://images.unsplash.com/photo-1455098934982-64c622c5e066") // URL("https://mdn.mozillademos.org/files/5397/rhino.jpg")
                val fs = FileOutputStream(file)
                url.openStream().use { input ->
                    fs.use { output ->
                        input.copyTo(output)
                    }
                }
                val image = BitmapFactory.decodeFile(file.absolutePath)
                ctx.drawImage(image, 0f, 0f)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun drawTriangle(ctx: CanvasRenderingContext2D) {
        ctx.beginPath();
        ctx.moveTo(20f, 140f);   // Move pen to bottom-left corner
        ctx.lineTo(120f, 10f);   // Line to top corner
        ctx.lineTo(220f, 140f);  // Line to bottom-right corner
        ctx.closePath();       // Line to bottom-left corner
        ctx.stroke();
    }

    fun drawArcMDN(ctx: CanvasRenderingContext2D) {
        // Tangential lines
        ctx.beginPath();
        ctx.strokeStyle = CanvasColorStyle.Color(Color.GRAY);
        ctx.moveTo(200f, 20f);
        ctx.lineTo(200f, 130f);
        ctx.lineTo(50f, 20f);
        ctx.stroke();

// Arc
        ctx.beginPath();
        ctx.strokeStyle = CanvasColorStyle.Color(Color.BLACK);
        ctx.lineWidth = 5f;
        ctx.moveTo(200f, 20f);
        ctx.arcTo(200f, 130f, 50f, 20f, 40f);
        ctx.stroke();

// Start point
        ctx.beginPath();
        ctx.fillStyle = CanvasColorStyle.Color(Color.BLUE);
        ctx.arc(200f, 20f, 5f, 0f, ((2 * Math.PI).toFloat()));
        ctx.fill();

// Control points
        ctx.beginPath();
        ctx.fillStyle = CanvasColorStyle.Color(Color.RED);
        ctx.arc(200f, 130f, 5f, 0f, (2 * Math.PI).toFloat()); // Control point one
        ctx.arc(50f, 20f, 5f, 0f, (2 * Math.PI).toFloat());   // Control point two
        ctx.fill();
    }

    class KeyValue(val x: Float, val y: Float) {
        constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())
    }

    fun drawBezierCurveTo(ctx: CanvasRenderingContext2D) {
        // Define the points as {x, y}
        var start = KeyValue(50f, 20f)
        var cp1 = KeyValue(230f, 30f)
        var cp2 = KeyValue(150f, 80f)
        var end = KeyValue(250f, 100f)

// Cubic Bézier curve
        ctx.beginPath();
        ctx.moveTo(start.x, start.y);
        ctx.bezierCurveTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y);
        ctx.stroke();

// Start and end points
        ctx.fillStyle = CanvasColorStyle.Color(Color.BLUE);
        ctx.beginPath();
        ctx.arc(start.x, start.y, 5f, 0f, (2 * Math.PI).toFloat());  // Start point
        ctx.arc(end.x, end.y, 5f, 0f, (2 * Math.PI).toFloat());      // End point
        ctx.fill();

// Control points
        ctx.fillStyle = CanvasColorStyle.Color(Color.RED);
        ctx.beginPath();
        ctx.arc(cp1.x, cp1.y, 5f, 0f, (2 * Math.PI).toFloat());  // Control point one
        ctx.arc(cp2.x, cp2.y, 5f, 0f, (2 * Math.PI).toFloat());  // Control point two
        ctx.fill();
    }
}
