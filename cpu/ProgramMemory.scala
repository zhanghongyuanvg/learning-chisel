// program memory rom

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

// abstract representation of an instruction
class Instruction( opcodeSize: Int = 5, argSize: Int = 5 ) extends Bundle {
    val opcode = UInt(OUTPUT, width=opcodeSize)
    val arg1 = UInt(OUTPUT, width=argSize)
    val arg2 = UInt(OUTPUT, width=argSize)

    // magic?? Without this we get the following error:
    // Cannot auto-create constructor for picomipscpu.Instruction that requires arguments: List(int, int)
    // Parameterized Bundle class picomipscpu.Instruction needs cloneType method
    override def clone: this.type = {
        val res = new Instruction()
        res.opcode.dir = this.opcode.dir
        res.arg1.dir = this.arg1.dir
        res.arg2.dir = this.arg2.dir
        res.asInstanceOf[this.type]
    }
}

object Instruction { // give arg2 then arg1 to make double length things easier to read
    def apply( opcodeSize: Int, argSize: Int, opcode: Int, arg2: Int, arg1: Int ) : Instruction = {
        val ret = new Instruction( opcodeSize, argSize )
        ret.opcode := UInt(opcode)
        ret.arg1 := UInt(arg1)
        ret.arg2 := UInt(arg2)
        ret // return ret
    }
}

// implementation
class ProgramMemory( gprAddrLength: Int, pcLength: Int ) extends Module {
    val opcodeSize = log2Up( opcodes.numOps )
    val io = new Bundle {
        val address = UInt(INPUT, width=pcLength)
        val out = new Instruction( opcodeSize, gprAddrLength )
    }

    val programText = Array( // imediate decrement example from README
        Instruction(opcodeSize, gprAddrLength, opcodes.ldi, 30, 30),
        Instruction(opcodeSize, gprAddrLength, opcodes.ld, 0, 1),
        Instruction(opcodeSize, gprAddrLength, opcodes.subi, 1, 1),
        Instruction(opcodeSize, gprAddrLength, opcodes.ld, 0, 1), // end of that

        Instruction(opcodeSize, gprAddrLength, opcodes.ldi, 20, 30), // addressed addition aexample
        Instruction(opcodeSize, gprAddrLength, opcodes.ld, 0, 1),
        Instruction(opcodeSize, gprAddrLength, opcodes.ldi, 10, 0),
        Instruction(opcodeSize, gprAddrLength, opcodes.ld, 0, 2),
        Instruction(opcodeSize, gprAddrLength, opcodes.add, 1, 2),
        Instruction(opcodeSize, gprAddrLength, opcodes.ld, 0, 1), // end of that
        Instruction(opcodeSize, gprAddrLength, opcodes.ji, 0, 0) // uncondiitonal jump
    )

    val rom = Vec( programText )

    io.out := rom( io.address )
}

// TODO tests
