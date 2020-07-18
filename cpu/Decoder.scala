// Instruction decoder

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

// instructions
object opcodes {
    val numOps = 16
    val nop :: add :: addi :: sub :: subi :: and :: andi :: or :: ori :: xor :: xori :: not :: ld :: ldi:: jr :: ji :: Nil = Range(0, numOps).toList
}

class Decoder ( pcSize: Int = 16) extends Module {
    val io = new Bundle {
        val opcode =  UInt(INPUT, log2Up(opcodes.numOps) )
        val pcControl = UInt(OUTPUT, log2Up(PCmodes.numOpts))
        val aluControl = UInt(OUTPUT, log2Up(ALUops.numOpts))
        val aluFlags = new ALUflags().flip
        val registersWriteEnable = Bool(OUTPUT)
        val immediate = Bool(OUTPUT)
        val longArg = Bool(OUTPUT)
    }

    // default values
    io.pcControl := UInt(PCmodes.increment)
    io.aluControl := UInt(ALUops.nop)
    io.registersWriteEnable := Bool(false)
    io.immediate := Bool(false)
    io.longArg := Bool(false)

    switch (io.opcode) {
        is (UInt(opcodes.nop)) {
            // do nothing
        }
        is (UInt(opcodes.add)) {
            io.aluControl := UInt(ALUops.add)
        }
        is  (UInt(opcodes.addi)) {
            io.aluControl := UInt(ALUops.add)
            io.immediate := Bool(true)
        }
        is (UInt(opcodes.sub)) {
            io.aluControl := UInt(ALUops.sub)
        }
        is (UInt(opcodes.subi)) {
            io.aluControl := UInt(ALUops.sub)
            io.immediate := Bool(true)
        }
        is (UInt(opcodes.and)) {
            io.aluControl := UInt(ALUops.and)
        }
        is (UInt(opcodes.andi)) {
            io.aluControl := UInt(ALUops.and)
            io.immediate := Bool(true)
        }
        is (UInt(opcodes.or)) {
            io.aluControl := UInt(ALUops.or)
        }
        is (UInt(opcodes.ori)) {
            io.aluControl := UInt(ALUops.or)
            io.immediate := Bool(true)
        }
        is (UInt(opcodes.xor)) {
            io.aluControl := UInt(ALUops.xor)
        }
        is (UInt(opcodes.xori)) {
            io.aluControl := UInt(ALUops.xor)
            io.immediate := Bool(true)
        }
        is (UInt(opcodes.not)) {
            io.aluControl := UInt(ALUops.notA)
        }
        is (UInt(opcodes.ld)) {
            io.registersWriteEnable := Bool(true)
        }
        is (UInt(opcodes.ldi)) {
            io.aluControl := UInt(ALUops.loadB)
            io.immediate := Bool(true)
            io.longArg := Bool(true)
        }
        is (UInt(opcodes.jr)) {
            io.pcControl := UInt(PCmodes.relativeJump)
            io.longArg := Bool(true)
        }
        is (UInt(opcodes.ji)) {
            io.pcControl := UInt(PCmodes.absoluteJump)
            io.longArg := Bool(true)
        }
    }
}

// testbench
class decoderTests (dut: Decoder) extends Tester(dut) {
    //TODO I am lazy
}

// boilerplate
object decoder {
    def main(args: Array[String]): Unit = {
        chiselMainTest(Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
            () => Module(new Decoder())){c => new decoderTests(c)}
  }
}

