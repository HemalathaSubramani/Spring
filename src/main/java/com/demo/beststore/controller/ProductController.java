package com.demo.beststore.controller;

import java.nio.file.Files;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.demo.beststore.models.Product;
import com.demo.beststore.models.ProductDto;
import com.demo.beststore.services.ProductRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {
	
	@Autowired
	private ProductRepository repo;
	
	//Model object is a part of the Spring MVC framework. It's used to pass data from your controller to your view (e.g., a Thymeleaf
	
	@GetMapping({"", "/"})
	public String showProductList(Model model) {
		List<Product> products = repo.findAll(Sort.by(Sort.Direction.ASC, "id") );
		model.addAttribute("products", products);
		return "products/index";
	}
	
	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute("productDto", productDto);
		return "products/createProduct";
	}
	
	@PostMapping({"/create"})
	public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {
		if(productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto","imageFile","The image is required"));
		}
		if(result.hasErrors()) {
			return "products/createProduct";
		}
		
		//save image field
		MultipartFile image = productDto.getImageFile();
		Date createdAt = new Date();
		String storageFileName = createdAt.getTime()+"_"+image.getOriginalFilename();
		
		try
		{
			String uploadDir = "public/images/";
			Path uploadPath = Paths.get(uploadDir);
			
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			try(InputStream inputStream = image.getInputStream()){
				Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
						StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception"+e.getMessage());
		}
		
		//save images and all in databases
		Product product = new Product();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setCreatedAt(createdAt);
		product.setImageFileName(storageFileName);
		
		repo.save(product);
		return "redirect:/products";
	}
	
	@GetMapping("/edit")
	public String showEditPage(Model model, @RequestParam int id)
	{
		try
		{
			Product product = repo.findById(id).get();
			model.addAttribute("product",product);
			
			ProductDto productDto = new ProductDto();
			productDto.setName(product.getName());
			productDto.setBrand(product.getBrand());
			productDto.setCategory(product.getCategory());
			productDto.setPrice(product.getPrice());
			productDto.setDescription(product.getDescription());
			
			model.addAttribute("productDto",productDto);
		}
		catch(Exception e)
		{
			System.out.println("Exception1"+e.getMessage());
			return "redirect:/products";
		}
		return "products/EditProduct";
	}
	
	 @PostMapping("/edit")
	 public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result)
	 {
		try
		{
			Product product = repo.findById(id).get();
			model.addAttribute("product", product);
			
			if(result.hasErrors()) {
				return "products/EditProduct";
			}
			
			if(!productDto.getImageFile().isEmpty())
				//delete old image
			{
				String uploadDir = "public/images";
				Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
				
				try
				{
					Files.delete(oldImagePath);
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
				
				//save new image
				MultipartFile image = productDto.getImageFile();
				Date createdAt = new Date();
				String storageFileName = createdAt.getTime()+"_"+image.getOriginalFilename();
				

				try(InputStream inputStream = image.getInputStream()){
					Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
							StandardCopyOption.REPLACE_EXISTING);
				}
				product.setImageFileName(storageFileName);
			}
			//again to dislay as it is to set it in the front view
			product.setName(productDto.getName());
			product.setBrand(productDto.getBrand());
			product.setCategory(productDto.getCategory());
			product.setPrice(productDto.getPrice());
			product.setDescription(productDto.getDescription());
			
			repo.save(product);
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		 return "redirect:/products";
	 }

	 @GetMapping("/delete")
	 public String deleteProduct(@RequestParam int id)
	 {
		 try
		 {
			 Product product = repo.findById(id).get();
			 
			 //delete the product image
			 Path imagePath = Paths.get("public/images" + product.getImageFileName());
			  
			 try {
				 Files.delete(imagePath);
			 }
			 catch(Exception ew)
			 {
				 System.out.println(ew.getMessage());
			 }
			 repo.delete(product);
		 }
		 catch(Exception ex)
		 {
			 System.out.println(ex.getMessage());
		 }
		 return "redirect:/products";
	 }
}
