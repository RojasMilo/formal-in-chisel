/**********************************************************
*	Name:  				ALU
*
*	Author: 			Hanssel Norato-Sep 17, 2019.
*
*	Modified by: 	Camilo Rojas-Aug 25, 2021.
*
* Abstract:	 		This module implement all the Arithmetic and
*								logical operations requiered by the spec
***********************************************************/

package ALU

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.stage.ChiselStage


object ALU_Codes{
	val number = Enum(16)
	val sll :: srl :: sra :: add :: sub :: xor :: or :: and :: slt :: sltu :: eq :: neq :: grt :: grtu :: nop1 :: nop2 :: Nil = number.slice(0,16)

}

class ALU (val formal:Boolean=false) extends Module {
	val io = IO(new Bundle {
		val sel = Input(UInt(5.W))
		val in1 = Input(UInt(32.W))
		val in2 = Input(UInt(32.W))
		val out = Output(UInt(32.W))
		val ovf = Output(UInt(1.W))
		val ans = Output(UInt(1.W))
	})

// shift

val shifted = Wire(UInt(32.W))
shifted := io.in1 >> io.in2(4,0)

// addition

val totalsum = Wire(UInt(33.W))
totalsum := io.in1 + io.in2

// substraction

val totalsub = Wire(UInt(33.W))
totalsub := io.in1 - io.in2


val code = ALU_Codes

io.out := 0.U
io.ovf := 0.U
io.ans := 0.U

	switch(io.sel){

		is(code.sll){    // shift left
			io.out := io.in1 << io.in2(4,0)
		}

		is(code.srl){    // shift right
			io.out := io.in1 >> io.in2(4,0)
		}

		is(code.sra){    // shift arithmetic right
			when     (io.in1(31) === 0.U){ io.out := shifted }
			.otherwise{ io.out := shifted | ("hFFFF_FFFF".U << (32.U - io.in2(4,0))) }
		}

		is(code.add){    // ADD
			io.out := totalsum(31,0)
			io.ovf := totalsum(32)
		}

		is(code.sub){    // SUB
			io.out := totalsub(31,0)
			io.ovf := totalsub(32)
		}

		is(code.xor){    // XOR
			io.out := io.in1 ^ io.in2
		}

		is(code.or){    // OR
			io.out := io.in1 | io.in2
		}

		is(code.and){    // AND
			io.out := io.in1 & io.in2
		}

		is(code.slt){    // smaller than
			io.out := (io.in1.asSInt < io.in2.asSInt).asUInt
			io.ans := (io.in1.asSInt < io.in2.asSInt).asUInt
		}

		is(code.sltu){    // smaller than unsigned
			io.out := Mux(io.in1 < io.in2, 1.U, 0.U)
			io.ans := Mux(io.in1 < io.in2, 1.U, 0.U)
		}

		is(code.eq){	// equal to
			io.out := Mux(io.in1 === io.in2, 1.U, 0.U)
			io.ans := Mux(io.in1 === io.in2, 1.U, 0.U)
		}

		is(code.neq){	// not equal to
			io.out := Mux(io.in1 === io.in2, 0.U, 1.U)
			io.ans := Mux(io.in1 === io.in2, 0.U, 1.U)
		}

		is(12.U){	// greater or equal to
			io.out := (io.in1.asSInt < io.in2.asSInt).asUInt  ^ 1.U
			io.ans := (io.in1.asSInt < io.in2.asSInt).asUInt  ^ 1.U
		}

		is(13.U){   // greater or equal to unsigned
			io.out := Mux(io.in1 < io.in2, 0.U, 1.U)
			io.ans := Mux(io.in1 < io.in2, 0.U, 1.U)
		}

		is(code.nop1){ io.out := io.in1 }

		is(code.nop2){ io.out := io.in2 }

	}

	if (formal){

		val init = RegInit(false.B)
		when (!init){
			verification.assume(reset.asBool)
			//verification.assert(io.out === 0.U)
			init := true.B
		}.elsewhen(io.sel === 15.U){
			verification.assert(io.out === io.in2)
		}.elsewhen((io.in1 <= "hFFFFFFFF".asUInt ) && (io.in2 <= "hFFFFFFFF".asUInt)){
			verification.assert(io.ovf === 0.U)
		}
	}
}

// Verilog Generation

object ALUDriver_Verilog extends App {
	  (new ChiselStage).emitVerilog(new ALU(formal=false),args)
}

// SystemVerilog Generation

object ALUDriver_SystemVerilog extends App {
  (new ChiselStage).emitSystemVerilog(new ALU(formal=true),args)
}
