// encapsulating module for the cpu

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

// TODO: automatic pcSize
class CPU (pcSize: Int = 8, gprAddrSize: Int = 5) extends Module {
    val wordSize = 2*gprAddrSize // see instruction documentation in README for ldi and add  
    val io = new Bundle {
        val out = UInt(OUTPUT, width=wordSize) // dummy output connected to the ALU's output so that we have something to test
    }

    // instances of sub-modules
    val alu = Module( new ALU( wordSize ) )
    val gpr = Module( new Registers( wordSize, gprAddrSize ) )
    val pc = Module( new ProgramCounter( pcSize ) )
    val decoder = Module( new Decoder( pcSize ) )
    val programROM = Module( new ProgramMemory( gprAddrSize, pcSize ) )

    // multiplexer which works out the imediate value given in the instruction
    // this is nessecarry because in instructions like addi, arg2 will probably be non-zero
    var longArgMux = Mux( decoder.io.longArg, programROM.io.out.arg2 ## programROM.io.out.arg1, programROM.io.out.arg1 )
    // multiplexer controlling if we are looking at an immediate value or a value from a register
    var imediateMux = Mux( decoder.io.immediate, longArgMux, gpr.io.read2.data )

    // wiring
    alu.io.control := decoder.io.aluControl
    alu.io.dataA := gpr.io.read1.data
    alu.io.dataB := imediateMux

    programROM.io.address := pc.io.instruct.pcOut

    pc.io.instruct.branchAddr := programROM.io.out.arg2 ## programROM.io.out.arg1 //concatination
    pc.io.mode := decoder.io.pcControl

    decoder.io.opcode := programROM.io.out.opcode
    decoder.io.aluFlags := alu.io.flags

    gpr.io.read1.address := programROM.io.out.arg1
    gpr.io.read2.address := programROM.io.out.arg2
    gpr.io.write.address := programROM.io.out.arg1
    gpr.io.write.data := alu.io.result
    gpr.io.writeEnable := decoder.io.registersWriteEnable

    // output the ALU's result for now
    io.out := alu.io.result
}

// testbench
class cpuTests (dut: CPU) extends Tester(dut) {
    // test imediate
    step(1) // ldi 6
    expect( dut.io.out, 990 ) 

    step (2) // ld 0; subi 0 1
    expect( dut.io.out, 989 )
    
    step(7) // run second test
    expect( dut.io.out, 990 )

    step(1) // jump

    // we should have jumped back to the beginning so do the same tests again
    // test imediate
    step(1) // ldi 6
    expect( dut.io.out, 990 ) 

    step (2) // ld 0; subi 0 1
    expect( dut.io.out, 989 )
    
    step(7) // run second test
    expect( dut.io.out, 990 )
}


// boilerplate
object cpu {
    def main(args: Array[String]): Unit = {
        chiselMainTest(Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
            () => Module(new CPU())){c => new cpuTests(c)}
  }
}

