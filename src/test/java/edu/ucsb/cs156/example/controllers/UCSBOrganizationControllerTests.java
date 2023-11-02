package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsborganization/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationRepository.findById(eq("XCX"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgcode=XCX"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("XCX"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with id XCX not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganization() throws Exception {

                // arrange

                UCSBOrganization zpr = UCSBOrganization.builder()
                                .orgTranslation("ZETA PHI RHO")
                                .orgcode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                UCSBOrganization sky = UCSBOrganization.builder()
                                .orgTranslation("SKYDIVING CLUB AT UCSB")
                                .orgcode("SKY")
                                .orgTranslationShort("SKYDIVING CLUB")
                                .inactive(false)
                                .build();

                ArrayList<UCSBOrganization> expectedCommons = new ArrayList<>();
                expectedCommons.addAll(Arrays.asList(zpr, sky));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedCommons);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedCommons);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbOrganization...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organization() throws Exception {
                // arrange

                UCSBOrganization osli = UCSBOrganization.builder()
                                .orgTranslation("OFFICEOFSTUDENTLIFE")
                                .orgcode("OSLI")
                                .orgTranslationShort("STUDENTLIFE")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.save(eq(osli))).thenReturn(osli);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganization/post?orgTranslation=OFFICEOFSTUDENTLIFE&orgcode=OSLI&orgTranslationShort=STUDENTLIFE&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(osli);
                String expectedJson = mapper.writeValueAsString(osli);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/ucsborganization?...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?orgcode=zpr"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganization orgs = UCSBOrganization.builder()
                                .orgTranslation("ZETA PHI RHO")
                                .orgcode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("zpr"))).thenReturn(Optional.of(orgs));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgcode=zpr"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("zpr"));
                String expectedJson = mapper.writeValueAsString(orgs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        // Tests for DELETE /api/ucsbdiningcommons?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                UCSBOrganization krc = UCSBOrganization.builder()
                                .orgTranslation("KOREAN RADIO CLUB")
                                .orgcode("KRC")
                                .orgTranslationShort("KOREAN RADIO CL")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("KRC"))).thenReturn(Optional.of(krc));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgcode=KRC")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("KRC");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id KRC deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationRepository.findById(eq("XCX"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgcode=XCX")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("XCX");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id XCX not found", json.get("message"));
        }

        //Tests for PUT /api/ucsborganization?...

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {
                // arrange

                UCSBOrganization orgOrg = UCSBOrganization.builder()
                                .orgTranslation("ZETA PHI RHO")
                                .orgcode("ZPR")
                                .orgTranslationShort("ZETA PHI RHO")
                                .inactive(false)
                                .build();

                UCSBOrganization orgEdit = UCSBOrganization.builder()
                                .orgTranslation("Ppppppp")
                                .orgcode("ZPR")
                                .orgTranslationShort("Zzzzzz")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(orgEdit);

                when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(orgOrg));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgcode=ZPR")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("ZPR");
                verify(ucsbOrganizationRepository, times(1)).save(orgEdit); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganization editedCommons = UCSBOrganization.builder()
                                .orgTranslation("Xm Cm Xm")
                                .orgcode("XCX")
                                .orgTranslationShort("Xm Cm Xm")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(editedCommons);

                when(ucsbOrganizationRepository.findById(eq("XCX"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgcode=XCX")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("XCX");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id XCX not found", json.get("message"));

        }
        
}
