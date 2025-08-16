import kr.ac.kaist.swrc.jhannanum.comm.PlainSentence
import kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.SentenceSegmentor.SentenceSegmentor
import kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.SentenceSegmentor2.SentenceSegmentor2
import kotlin.test.Test

class SentenceSegmentorTest {
    @Test
    fun testSegmentor() {
        val ceg1 = SentenceSegmentor()
        val ceg2 = SentenceSegmentor2()

        val te = PlainSentence(1,1,false,
            "12323.23 아름다운 우리나라 금수강산에서 살자! 그러나 나는 뭐가 중한지 모르겠다.")

        var fst1 = ceg1.doProcess(te)
        var fst2 = ceg2.doProcess(te)

        while( fst1 != null){
            println(fst1.sentence)
            //println(fst.documentID)
            //println(fst.sentenceID)
            fst1 = ceg1.doProcess(null)
        }

        while( fst2 != null){
            println(fst2.sentence)
            //println(fst.documentID)
            //println(fst.sentenceID)
            fst2 = ceg2.doProcess(null)
        }
    }
}