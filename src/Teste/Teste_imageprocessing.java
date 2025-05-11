package Teste;

import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import Activité1.MainTraitementImage;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class Teste_imageprocessing {
	static Path currentDirPath = Paths.get("");
	public static String currentDir = currentDirPath.toAbsolutePath().toString().replace("\\", "/");

	  @Test public void testp1(){ 
		  List<Integer> val=new ArrayList<Integer>();
	  val=MainTraitementImage.img("p1.jpg") ; 
	  Integer comp=val.get(0); 
	  System.out.println(comp);
	  assertTrue(comp.equals(2)); }
	  
	  @Test public void testp2(){ 
	List<Integer> val=new ArrayList<Integer>();
	  val=MainTraitementImage.img("p2.jpg") ;
	  Integer comp=val.get(0); 
	  System.out.println(comp); 
	  assertTrue(comp.equals(2));
	  
	  
	  }
	  @Test public void testp3(){ 
		 List<Integer> val=new ArrayList<Integer>();
	  val=MainTraitementImage.img("Screenshot_6.png") ; 
	  Integer comp=val.get(0); 
	  System.out.println(comp); 
	  assertTrue(comp.equals(4));
	  
	  
	  }
	  
	  @Test public void testp4(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p3.jpg") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(4));
		  
		  
		  }
	  @Test public void testp5(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p5.jpg") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(4));
		  
		  
		  }
	  @Test public void testp6(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p6.jpg") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(0));
		  
		  
		  }
	  
	  @Test public void testp7(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p7.jpg") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(1));
		  
		  
		  }
	  @Test public void testp8(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p8.jpg") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(0));
		  
		  
		  }
	  @Test public void testp9(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p9.jpg") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(2));
		  
		  
		  }
	  @Test public void testp10(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p10.jpg") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(1));
		  
		  
		  }
	  
	  @Test public void testp11(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p11.png") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(5));
		  
		  
		  }
	  @Test public void testp12(){ 
			 List<Integer> val=new ArrayList<Integer>();
		  val=MainTraitementImage.img("p12.png") ; 
		  Integer comp=val.get(0); 
		  System.out.println(comp); 
		  assertTrue(comp.equals(3));
		  
		  
		  }
    
}
