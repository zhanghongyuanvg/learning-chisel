// ALU for the CPU

/*  This file is part of picomips-cpu.

    picomips-cpu is a free hardware design: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    picmips-cpu is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with picomips-cpu.  If not, see http://www.gnu.org/licenses/.*/

package picomipscpu
import Chisel._

// possible values for control signals
object ALUops {
    val numOpts = 8
    val loadB :: add :: sub :: and :: or :: xor :: notA :: nop :: Nil = Range(0, numOpts).toList
}

// ALU flags interface
class ALUflags extends Bundle {
    val zero = Bool( OUTPUT )
    // TODO: negative, carry
}

// ALU implementation
class ALU (wordSize: Int) extends Module {
    val io = new Bundle {
        val control = UInt( INPUT, log2Up(ALUops.numOpts) )
        val dataA = SInt( INPUT, wordSize )
        val dataB = SInt( INPUT, wordSize )
        val result = UInt( OUTPUT, wordSize )
        val flags = new ALUflags()
    }
    val acc = Reg(UInt(width=wordSize))
    io.result := acc

    // work out io.result
    switch (io.control) {
        is (UInt(ALUops.loadB)) {
            acc := io.dataB
        } 
        is (UInt(ALUops.add)) {
            acc := io.dataA + io.dataB
        } 
        is (UInt(ALUops.sub)) {
            acc := io.dataA - io.dataB
        } 
        is (UInt(ALUops.and)) {
            acc := io.dataA & io.dataB
        } 
        is (UInt(ALUops.or)) {
            acc := io.dataA | io.dataB
        } 
        is (UInt(ALUops.xor)) {
            acc := io.dataA ^ io.dataB
        } 
        is (UInt(ALUops.notA)) {
            acc := ~io.dataA
        } 
        is (UInt(ALUops.nop)) {
            // do nothing   
        }
    }

    // work out io.flags.zero
    when (io.result === UInt(0)) {
        io.flags.zero := Bool(true)
    } .otherwise {
        io.flags.zero := Bool(false)
    }
}

// testbench
class ALUtests (dut: ALU) extends Tester(dut) {
    // loadB
    poke( dut.io.control, ALUops.loadB )
    poke( dut.io.dataB, 10 )
    step(1)
    expect( dut.io.result, 10 )
    expect( dut.io.flags.zero, 0 )

    // add
    poke( dut.io.control, ALUops.add )
    poke( dut.io.dataA, 1 )
    poke( dut.io.dataB, 2 )
    step(1)
    expect( dut.io.result, 3 )
    expect( dut.io.flags.zero, 0 )

    // sub
    poke( dut.io.control, ALUops.sub )
    poke( dut.io.dataA, 12 )
    poke( dut.io.dataB, -3 )
    step(1)
    expect( dut.io.result, 15 )
    expect( dut.io.flags.zero, 0 )

    // and
    poke( dut.io.control, ALUops.and )
    poke( dut.io.dataA, 4 )
    poke( dut.io.dataB, 2 )
    step(1)
    expect( dut.io.result, 0 )
    expect( dut.io.flags.zero, 1 )

    // or
    poke( dut.io.control, ALUops.or )
    poke( dut.io.dataA, 2 )
    poke( dut.io.dataB, 4 )
    step(1)
    expect( dut.io.result, 6 )
    expect( dut.io.flags.zero, 0 )

    // xor
    poke( dut.io.control, ALUops.xor )
    poke( dut.io.dataA, 6 )
    poke( dut.io.dataB, 4 )
    step(1)
    expect( dut.io.result, 2 )
    expect( dut.io.flags.zero, 0 )

    // notA
    poke( dut.io.control, ALUops.notA )
    poke( dut.io.dataA, 100 )
    step(1)
    expect( dut.io.result, 0xff9b ) // this will break if you change the testWordSize!!
    expect( dut.io.flags.zero, 0 )

    // nop
    poke( dut.io.control, ALUops.nop )
    step(1)
    expect( dut.io.result, 0xff9b )
    expect( dut.io.flags.zero, 0 )
}

// boilerplate
object alu {
    val testWordSize = 16 // if you change this you will need ot change the tests for notA and notB
    def main(args: Array[String]): Unit = {
        chiselMainTest(Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
            () => Module(new ALU(testWordSize))){c => new ALUtests(c)}
  }
}
