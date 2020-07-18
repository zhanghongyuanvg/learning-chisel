// gneral purpose reisters
// this can read from two registers in a clock cycle or write to one.
// reading from address 0 always results in a zero (just part of picomips)
// behaviour when reading and writing to the same address on the same clock edge is undefined

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

// register channel interfaces 
class registerReadChannel (wordSize: Int, addressSize: Int) extends Bundle {
    val address = UInt( INPUT, width = addressSize )
    val data = Bits( OUTPUT, width = wordSize )
}

class registerWriteChannel (wordSize: Int, addressSize: Int) extends Bundle {
    val address = UInt( INPUT, width = addressSize )
    val data = Bits( INPUT, width = wordSize )
}

// Registers implementation
// TODO: configurable numbers of read/write channels?
class Registers (wordSize: Int = 8, addressSize: Int = 5) extends Module {
    val io = new Bundle {
        val writeEnable = Bool( INPUT )
        val read1 = new registerReadChannel(wordSize, addressSize)
        val read2 = new registerReadChannel(wordSize, addressSize)
        val write = new registerWriteChannel(wordSize, addressSize)
    }
    val numAddresses = scala.math.pow(2, addressSize).round.toInt // integer to the power integer should always be a whole number. I am willing to assume that the representation of a double is accurate ot one decimal place so that the rounding is done correctly
    val mem = Mem(n = numAddresses, UInt(width = wordSize))

    // write
    when (io.writeEnable) { 
        mem(io.write.address) := io.write.data 
    }
  
    // read
    io.read1.data := UInt(0)
    when (io.read1.address != UInt(0)) { 
        io.read1.data := mem(io.read1.address) 
    }

    io.read2.data := UInt(0)
    when (io.read2.address != UInt(0)) {
        io.read2.data := mem(io.read2.address) 
    }
}

// testbench
class RegistersTests (dut: Registers) extends Tester(dut) {
    // write something
    poke( dut.io.writeEnable, 1)
    poke( dut.io.write.address, 1)
    poke( dut.io.write.data, 5)
    poke( dut.io.read1.address, 0)
    poke( dut.io.read2.address, 0)
    step(1)
    expect( dut.io.read1.data, 0)
    expect( dut.io.read2.data, 0)
    
    // read it back and try writing to address 0
    poke( dut.io.writeEnable, 1 )
    poke( dut.io.read1.address, 0 )
    poke( dut.io.read2.address, 1 )
    poke( dut.io.write.address, 0 )
    poke( dut.io.write.data, 2 )
    step(1)
    expect( dut.io.read1.data, 0 )
    expect( dut.io.read2.data, 5 )
}

// boilerplate
object Registers {
    val testWordSize = 8
    val testAddressSize = 2
    
def main(args: Array[String]): Unit = {
        chiselMainTest(Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
            () => Module(new Registers(testWordSize, testAddressSize))){c => new RegistersTests(c)}
  }
}

