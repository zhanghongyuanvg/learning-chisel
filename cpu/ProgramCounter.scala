// Program counter
// normally this will just increment but it will also need to implement branching

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

// modes of operatoin for the PC
object PCmodes {
    val numOpts = 3
    val increment :: relativeJump :: absoluteJump :: Nil = Range(0, numOpts).toList
}

// interface to instruction memory
class pcToInsruct(pcSize: Int = 16) extends Bundle {
    val branchAddr = UInt(INPUT, pcSize)
    val pcOut = UInt(OUTPUT, pcSize)
}

// implementation
class ProgramCounter(pcSize: Int = 16) extends Module {
    val io = new Bundle {
        val mode = UInt(INPUT, log2Up(PCmodes.numOpts))
        val instruct = new pcToInsruct(pcSize)
    }

    // register
    val pcNext = UInt(width=pcSize)
    val PC = Reg(outType=UInt(width=pcSize), next = pcNext, init = UInt(0))
    io.instruct.pcOut := PC

    // default that should never happen
    pcNext := UInt(0)

    // things that should actually happen
    switch (io.mode) {
        is (UInt(PCmodes.increment)) {
            pcNext := io.instruct.pcOut + UInt(1)
        }
        is (UInt(PCmodes.relativeJump)) {
            pcNext := io.instruct.pcOut + io.instruct.branchAddr
        }
        is (UInt(PCmodes.absoluteJump)) {
            pcNext := io.instruct.branchAddr
        }
    }
}

// testbench
class PCtests (dut: ProgramCounter) extends Tester(dut) {
    // do we start with PC = 0
    expect( dut.io.instruct.pcOut, 0)

    // increment the PC
    poke( dut.io.mode, PCmodes.increment )
    step(1)
    expect( dut.io.instruct.pcOut, 1)

    // relative jump
    poke( dut.io.instruct.branchAddr, 2 )
    poke( dut.io.mode, PCmodes.relativeJump)
    step(1)
    expect( dut.io.instruct.pcOut, 3 )

    // absolute jump
    poke( dut.io.instruct.branchAddr, 2 )
    poke( dut.io.mode, PCmodes.absoluteJump)
    step(1)
    expect( dut.io.instruct.pcOut, 2 ) 
}

// boilerplate
object ProgramCounter {
    val testPCsize = 16
    def main(args: Array[String]): Unit = {
        chiselMainTest(Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
            () => Module(new ProgramCounter(testPCsize))){c => new PCtests(c)}
  }
}
    
