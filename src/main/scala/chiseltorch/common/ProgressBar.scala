package chiseltorch.common

class ProgressBar(max: Int) {
    def update(progress: Int) {
        val percent = (progress.toDouble / max.toDouble) * 100
        val bar = ("=" * (percent / 2).toInt) + ">"
        val spaces = " " * (50 - bar.length)
        print("\r[" + bar + spaces + "] " + percent.toInt + "%")
    }

    def finished() {
        update(max)
        println()
    }
}

object ProgressBarTest extends App {
    val pb = new ProgressBar(100)
    for (i <- 1 to 50) {
        pb.update(i)
        Thread.sleep(100)
    }
    pb.finished()
}
