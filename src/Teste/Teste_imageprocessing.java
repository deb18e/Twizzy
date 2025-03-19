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

}
