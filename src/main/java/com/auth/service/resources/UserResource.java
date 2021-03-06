package com.auth.service.resources;

import com.auth.service.dao.UserDAO;
import com.auth.service.entities.User;
import com.auth.service.token.JWTSignerService;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.hibernate.UnitOfWork;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/user")
@Api("/v1/user")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserResource {

    private final UserDAO userDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    public UserResource(UserDAO userDao) {
        this.userDao = userDao;
    }

    @POST
    @Path("/save")
    @ApiOperation("Save user")
    @UnitOfWork
    @Timed
    public Response find(@Valid User user) {
        user.setRegisteredDate(new Date(System.currentTimeMillis()));
        userDao.save(user);
        return Response.ok().entity(user).type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
    }

    @GET
    @Path("/all")
    @ApiOperation("Find all")
    @UnitOfWork
    @Timed
    public Response findAll() {
        List<User> allUsers = userDao.findAll();
        return Response.ok().entity(allUsers).type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
    }

    @GET
    @Path("/one/{token}/{id}")
    @ApiOperation("Find by id")
    @UnitOfWork
    @Timed
    public Response findOne(@PathParam("token") String token, @PathParam("id") String id) {
        final User user = userDao.findById(id);
        System.out.println("found user  " + user);
        Claims claims = JWTSignerService.verifyJWT(token, user.getId(), user.getEmail());
        if (claims.getId().equalsIgnoreCase(user.getId())) {
           System.out.println("FOUND USER " + user.toString());
           return Response.ok().entity(user).type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
        } 
        return Response.serverError().entity(new User()).type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
    }

    @POST
    @Path("/login")
    @ApiOperation("User Login")
    public Response userAuth(@Valid User data) {
        Optional<User> model = userDao.login(data.getEmail(), data.getPassword());
        if (model.isPresent()) {
            String token = JWTSignerService.createJWTToken(model.get().getId(), model.get().getEmail(), model.get().getEmail(), System.currentTimeMillis());
            LOGGER.info("Generated New Token  " + token);
            System.out.println("model.get().getEmail()  " + model.get().getEmail());
            model.get().setToken(token);
            return Response.ok().entity(model.get()).type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
        }
        return Response.serverError().entity(new User()).type(MediaType.APPLICATION_JSON).build();
    }

    @DELETE
    @Path("/delete")
    @ApiOperation("Drop Collection")
    @UnitOfWork
    @Timed
    public Response drop() {
        userDao.remove();
        return Response.ok().entity("Ok").type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
    }
}
