import kr.ac.kaist.swrc.jhannanum.comm.PlainSentence
import kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.Spacing
import kotlin.test.Test

class SpacingTest {
    @Test
    fun testSpacing() {
        val sentences = arrayOf<String>(
            "우리집에어떻게왔는지모르겠지만너도뭘해야될지고민을해야될거아니냐?",
            "아버지가방에들어가신다",
            "나는밥을먹는다",
            "띄어쓰기가전혀안된문장입니다"
        )

        val ceg = Spacing()

        sentences.forEachIndexed { i, sentence ->
            ceg.initialize("", "")

            val te = PlainSentence(1, i, false, sentence)
            println(te.sentence)
            val fst = ceg.doProcess(te)
            println(fst.sentence)
            //println(fst.documentID)
            //println(fst.sentenceID)
        }
    }
}