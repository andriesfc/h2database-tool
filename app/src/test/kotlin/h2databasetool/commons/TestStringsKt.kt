package h2databasetool.commons

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe

class TestStringsKt : FunSpec({

    context("String.flattened()") {
        test("using margin (|)") {
            val expected =
                "EBUWMIBINFZUK3LQOR4SQKJJEBZGK5DVOJXCA5DINFZQUIBAEAQHMYLMEBWGS3TFOMQD2IDMNFXGKU3FOF2WK3TDMUUCSLTJORSXEYLUN5ZCQKIKEAQCAIDWMFZCA3DJNZSSAPJANRUW4ZLTFZXGK6DUFAUQUIBAEAQHMYLMEBWGKZTUJVQXEZ3JNYQD2IDXNBSW4IBIOZQWYIDJEA6SA3DJNZSS42LOMRSXQT3GFATXYJZJFEQHWCRAEAQCAIBAEAQC2MJAFU7CA3DJNZSS42LOMRUWGZLTFZ2GC23FK5UGS3DFEB5SA3DJNZSVW2LULUXGS42XNBUXIZLTOBQWGZJIFEQH2LTDN52W45BIFEQCWIBRBIQCAIBAEAQCAIDFNRZWKIBNHYQGSIBLEAYQUIBAEAQH2==="
            val given =
                """
                |EBUWMIBINFZUK3LQOR4SQKJJEBZGK5
                |DVOJXCA5DINFZQUIBAEAQHMYLMEBWG
                |S3TFOMQD2IDMNFXGKU3FOF2WK3TDMU
                |UCSLTJORSXEYLUN5ZCQKIKEAQCAIDW
                |MFZCA3DJNZSSAPJANRUW4ZLTFZXGK6
                |DUFAUQUIBAEAQHMYLMEBWGKZTUJVQX
                |EZ3JNYQD2IDXNBSW4IBIOZQWYIDJEA
                |6SA3DJNZSS42LOMRSXQT3GFATXYJZJ
                |FEQHWCRAEAQCAIBAEAQC2MJAFU7CA3
                |DJNZSS42LOMRUWGZLTFZ2GC23FK5UG
                |S3DFEB5SA3DJNZSVW2LULUXGS42XNB
                |UXIZLTOBQWGZJIFEQH2LTDN52W45BI
                |FEQCWIBRBIQCAIBAEAQCAIDFNRZWKI
                |BNHYQGSIBLEAYQUIBAEAQH2===
                |"""
            flattenStr(given) shouldBeEqual expected
        }
        test("using whitespace (with end trimmed)") {

            val expected =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Stet sadipscing at enim minim. Aliquyam suscipit amet."

            val given =
                """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                 Stet sadipscing at enim minim.   
                 Aliquyam suscipit amet.
                """

            val actual = given.flatten(trimEnd = true)

            actual shouldBe expected
        }

        test("using whitespace (not trimmed at end)") {
            val expected =
                "The Vivo X200 Pro is officially available in South Africa – a first for the Chinese company, which has thus far been reluctant to introduce its flagship line-up to South Africa. That all changed this evening at the company’s launch event in Midrand. There’s just one problem: taking Vivo’s flagship X200 Pro under your wing isn’t cheap, setting you back a whopping R40,000."

            val given = """
                The Vivo X200 Pro is officially available in South
                 Africa – a first for the Chinese company, which has
                 thus far been reluctant to introduce its flagship
                 line-up to South Africa. That all changed this
                 evening at the company’s launch event in Midrand.
                 There’s just one problem: taking Vivo’s flagship
                 X200 Pro under your wing isn’t cheap, setting
                 you back a whopping R40,000."""
                .flatten()

            val actual = given.flatten(trimEnd = false)
            actual shouldBe expected
        }
    }
})
