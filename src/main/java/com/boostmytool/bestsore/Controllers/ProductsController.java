package com.boostmytool.bestsore.Controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
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

import com.boostmytool.bestsore.models.Product;
import com.boostmytool.bestsore.models.Productdto;
import com.boostmytool.bestsore.services.productsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private productsRepository repo;

    @GetMapping({"","/"})
    public String showProductList(Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC,"id"));
        model.addAttribute("products", products);
        return "products/index";
    }
    
    @GetMapping("/create")
    public String showCreatePage(Model model) {
        Productdto productDto = new Productdto();
        model.addAttribute("productDto", productDto);
        return "products/CreateProduct";
    }
    
    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ("productDto") Productdto productDto,
            BindingResult result
    ) {
        if (productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is empty"));
        }
        if (result.hasErrors()) {
            return "products/CreateProduct";
        }

        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date(0);
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
        
        try {
            String uploadDir = "public/images";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
        Product product=new Product();
        product.setName(productDto.getName()); 
        product.setBrand(productDto.getBrand());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCategory(productDto.getCategory());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);
        
        repo.save(product);
        
        
        return "redirect:/products";
    }
    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            Productdto productDto = new Productdto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());
            
            model.addAttribute("productDto", productDto);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(Model model,@RequestParam int id,@Valid @ModelAttribute Productdto productDto,BindingResult result) {
    	try {
    		Product product=repo.findById(id).get();
    		model.addAttribute("product",product);
    		if(result.hasErrors()) {
    			return "product/EditProduct";
    		}
    		if(!productDto.getImageFile().isEmpty()) {
    			String uploadDir="public/images";
    			Path oldImagePath=Paths.get(uploadDir +product.getImageFileName());
    			try {
    				Files.delete(oldImagePath);
    				
    			}catch(Exception ex) {
    				System.out.println("Exception:"+ex.getMessage());
    			}
    			MultipartFile image =productDto.getImageFile();
    			Date createdAt= new Date(id);
    			String storageFileName=createdAt.getTime()+"_"+image.getOriginalFilename();
    			try(InputStream inputStream =image.getInputStream()) {
    				Files.copy(inputStream,Paths.get(uploadDir+storageFileName),
    						StandardCopyOption.REPLACE_EXISTING);
    				
    			}
    			product.setImageFileName(storageFileName);
    		}
    		product.setName(productDto.getName());
    		product.setBrand(productDto.getBrand());
    		product.setCategory(productDto.getCategory());
    		product.setPrice(productDto.getPrice());
    		product.setDescription(productDto.getDescription());
    		repo.save(product);
    		
    		
    	}catch(Exception ex) {
    		System.out.println(ex.getMessage());
    	}
    	
    	
    	return "redirect:/products";
    	
    }
    
   

    
    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
    	
        try {
        	Product product=repo.findById(id).get();
        	Path imagePath=Paths.get("public/images/"+product.getImageFileName());
        	try {
        		Files.delete(imagePath);
        	}catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }
        	repo.delete(product);
        	
           
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return "redirect:/products";
    }

    		
    
    		

    

}

