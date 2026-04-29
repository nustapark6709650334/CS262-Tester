package com.example.demo;                                                                    
                                                                                             
import com.example.demo.controller.ApiControllerN;                                           
import com.example.demo.controller.ApiControllerS;                                           
import com.example.demo.repository.CourseRepositoryN;                                        
import com.example.demo.repository.CourseRepositoryS;                                        
import org.junit.jupiter.api.BeforeEach;                                                     
import org.junit.jupiter.api.Test;                                                           
import org.junit.jupiter.api.extension.ExtendWith;                                           
import org.springframework.dao.DataAccessResourceFailureException;                           
import org.springframework.test.util.ReflectionTestUtils;                                    
import org.springframework.test.web.servlet.MockMvc;                                         
import org.springframework.test.web.servlet.setup.MockMvcBuilders;                           
import org.mockito.Mock;                                                                     
import org.mockito.junit.jupiter.MockitoExtension;                                           
                                                                                             
import java.util.Collections;                                                                
import java.util.Optional;                                                                   
                                                                                             
import static org.junit.jupiter.api.Assertions.assertThrows;                                 
import static org.mockito.Mockito.when;                                                      
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;       
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;      
                                                                                             
@ExtendWith(MockitoExtension.class)                                                          
class CourseErrorIntegrationTest {                                                           
                                                                                             
    private MockMvc mockMvc;                                                                 
                                                                                             
    @Mock                                                                                    
    private CourseRepositoryN courseRepositoryN;                                             
                                                                                             
    @Mock                                                                                    
    private CourseRepositoryS courseRepositoryS;                                             
                                                                                             
    @BeforeEach                                                                              
    void setUp() {                                                                           
        ApiControllerN apiControllerN = new ApiControllerN();                                
        ApiControllerS apiControllerS = new ApiControllerS();                                
                                                                                             
        ReflectionTestUtils.setField(apiControllerN, "courseRepositoryN", courseRepositoryN);
        ReflectionTestUtils.setField(apiControllerS, "courseRepositoryS", courseRepositoryS);
                                                                                             
        mockMvc = MockMvcBuilders                                                            
                .standaloneSetup(apiControllerN, apiControllerS)                             
                .build();                                                                    
    }                                                                                        
                                                                                             
    @Test                                                                                    
    void INT_EXT_05_missingParameterInCourseRequestShouldNotCrash() throws Exception {       
        /*                                                                                   
         * Current behaviour:                                                                
         * /api/coursesN does not require query parameter.                                   
         * If query is missing, controller calls findAll().                                  
         */                                                                                  
        when(courseRepositoryN.findAll()).thenReturn(Collections.emptyList());               
                                                                                             
        mockMvc.perform(get("/api/coursesN"))                                                
                .andExpect(status().isOk());                                                 
    }                                                                                        
                                                                                             
    @Test                                                                                    
    void INT_EXT_06_invalidCourseCodeInDetailRequestShouldReturnNotFound() throws Exception {
        when(courseRepositoryN.findById("INVALID_CODE"))                                     
                .thenReturn(Optional.empty());                                               
                                                                                             
        mockMvc.perform(get("/api/coursesN/INVALID_CODE"))                                   
                .andExpect(status().isNotFound());                                           
    }                                                                                        
                                                                                             
    @Test                                                                                    
    void INT_EXT_07_backendErrorPropagationFromRepositoryShouldBeDetected() {                
        when(courseRepositoryN.findById("DB_ERROR"))                                         
                .thenThrow(new RuntimeException("Simulated repository error"));              
                                                                                             
        assertThrows(Exception.class, () -> {                                                
            mockMvc.perform(get("/api/coursesN/DB_ERROR"));                                  
        });                                                                                  
    }                                                                                        
                                                                                             
    @Test                                                                                    
    void INT_EXT_08_dependencyUnavailableShouldBeDetected() {                                
        when(courseRepositoryS.findAll())                                                    
                .thenThrow(new DataAccessResourceFailureException("Database unavailable"));  
                                                                                             
        assertThrows(Exception.class, () -> {                                                
            mockMvc.perform(get("/api/coursesS"));                                           
        });                                                                                  
    }                                                                                        
}                                                                                            