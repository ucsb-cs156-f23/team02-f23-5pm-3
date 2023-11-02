package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;

import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {
    @MockBean
    HelpRequestRepository helpRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET /api/helprequests/all
        
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/helprequests/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/helprequests/all"))
                .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_helprequests() throws Exception {
        
            // arrange
            LocalDateTime rqt1 = LocalDateTime.parse("2022-04-20T17:35");

            HelpRequest helpRequest1 = HelpRequest.builder()
                            .requesterEmail("cgaucho@ucsb.edu")
                            .teamId("s22-5pm-3")
                            .tableOrBreakoutRoom("7")
                            .requestTime(rqt1)
                            .explanation("Need help with Swagger-ui")
                            .solved(false)
                            .build();

            LocalDateTime rqt2 = LocalDateTime.parse("2022-04-20T18:31");

            HelpRequest helpRequest2 = HelpRequest.builder()
                            .requesterEmail("ldelplaya@ucsb.edu")
                            .teamId("s22-6pm-3")
                            .tableOrBreakoutRoom("11")
                            .requestTime(rqt2)
                            .explanation("Dokku problems")
                            .solved(false)
                            .build();

            ArrayList<HelpRequest> expectedRequests = new ArrayList<>();
            expectedRequests.addAll(Arrays.asList(helpRequest1, helpRequest2));

            when(helpRequestRepository.findAll()).thenReturn(expectedRequests);

            // act
            MvcResult response = mockMvc.perform(get("/api/helprequests/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(helpRequestRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedRequests);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/ucsbdates/post...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/helprequests/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/helprequests/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_helprequest() throws Exception {
            // arrange

            LocalDateTime rqt3 = LocalDateTime.parse("2022-04-21T14:15");

            HelpRequest helpRequest3 = HelpRequest.builder()
                        .requesterEmail("pdg@ucsb.edu")
                        .teamId("s22-5pm-4")
                        .tableOrBreakoutRoom("13")
                        .requestTime(rqt3)
                        .explanation("Merge conflict")
                        .solved(true)
                        .build();

            when(helpRequestRepository.save(eq(helpRequest3))).thenReturn(helpRequest3);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/helprequests/post?requesterEmail=pdg@ucsb.edu&teamId=s22-5pm-4&tableOrBreakoutRoom=13&requestTime=2022-04-21T14:15&explanation=Merge conflict&solved=true")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(helpRequestRepository, times(1)).save(helpRequest3);
            String expectedJson = mapper.writeValueAsString(helpRequest3);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }
    // Tests for PUT /api/helprequests?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_helprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

                HelpRequest helpRequestOrig = HelpRequest.builder()
                        .requesterEmail("cgaucho@ucsb.edu")
                        .teamId("s22-5pm-3")
                        .tableOrBreakoutRoom("7")
                        .requestTime(ldt1)
                        .explanation("Need help with Swagger-ui")
                        .solved(false)
                        .build();

                HelpRequest helpRequestEdited = HelpRequest.builder()
                        .requesterEmail("ldelplaya@ucsb.edu")
                        .teamId("s22-6pm-3")
                        .tableOrBreakoutRoom("11")
                        .requestTime(ldt2)
                        .explanation("Dokku problems")
                        .solved(true)
                        .build();

                String requestBody = mapper.writeValueAsString(helpRequestEdited);

                when(helpRequestRepository.findById(eq(123L))).thenReturn(Optional.of(helpRequestOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequests?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(123L);
                verify(helpRequestRepository, times(1)).save(helpRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_helprequest_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest helpEditedRequest = HelpRequest.builder()
                        .requesterEmail("cgaucho@ucsb.edu")
                        .teamId("s22-5pm-3")
                        .tableOrBreakoutRoom("7")
                        .requestTime(ldt1)
                        .explanation("Need help with Swagger-ui")
                        .solved(true)
                        .build();

                String requestBody = mapper.writeValueAsString(helpEditedRequest);

                when(helpRequestRepository.findById(eq(123L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/helprequests?id=123")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(123L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 123 not found", json.get("message"));
        }
    // Tests for GET /api/helprequests?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/helprequests?id=123"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest helpRequest = HelpRequest.builder()
                        .requesterEmail("cgaucho@ucsb.edu")
                        .teamId("s22-5pm-3")
                        .tableOrBreakoutRoom("7")
                        .requestTime(ldt)
                        .explanation("Need help with Swagger-ui")
                        .solved(true)
                        .build();

                when(helpRequestRepository.findById(eq(123L))).thenReturn(Optional.of(helpRequest));

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequests?id=123"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findById(eq(123L));
                String expectedJson = mapper.writeValueAsString(helpRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange
                when(helpRequestRepository.findById(eq(123L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequests?id=123"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(eq(123L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("HelpRequest with id 123 not found", json.get("message"));
        }
}
