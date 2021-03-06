package com.example.SampleAPI.resources;

import com.example.SampleAPI.models.Cursor;
import com.example.SampleAPI.models.User;
import com.example.SampleAPI.representations.UserRepresentation;
import com.example.SampleAPI.representations.UsersRepresentation;
import com.example.SampleAPI.resources.beans.PaginationFilterBean;
import com.example.SampleAPI.services.UserService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;

import static com.example.SampleAPI.resources.UserResource.USER_COLLECTION_RESOURCE_PATH;
import static com.example.SampleAPI.resources.beans.PaginationFilterBean.CURSOR;
import static com.example.SampleAPI.resources.beans.PaginationFilterBean.NUMBER;

@Path(USER_COLLECTION_RESOURCE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource
{
    public final static String USER_COLLECTION_RESOURCE_PATH = "/users";
    public final static String USER_ITEM_RESOURCE_PATH = "/{profileName}";
    public final static String POST_COLLECTION_RESOURCE_PATH = USER_ITEM_RESOURCE_PATH + "/posts";
    public final static String PROFILE_NAME_PATH_PARAM = "profileName";

    @GET
    @Path(USER_ITEM_RESOURCE_PATH)
    public Response getUser(@Context UriInfo uriInfo, @PathParam(PROFILE_NAME_PATH_PARAM) String profileName)
    {
        User user = UserService.getUser(profileName);
        UserResource.addSelfAndPosts(uriInfo, user);

        return Response.ok().entity(new UserRepresentation(user)).build();
    }

    @GET
    public Response getAllUsers(@BeanParam PaginationFilterBean paginationFilterBean, @Context UriInfo uriInfo)
    {
        UsersRepresentation usersRepresentation;
        if(paginationFilterBean.getCursor() >= 0 && paginationFilterBean.getNumber() > 0)
        {
            ArrayList<User> users = UserService.getAllUsersWithPagination(paginationFilterBean.getCursor(), paginationFilterBean.getNumber());
            for(User user : users)
            {
                UserResource.addSelfAndPosts(uriInfo, user);
            }

            Cursor cursor;
            if(paginationFilterBean.getNumber() > users.size())
            {
                cursor = null;
            }
            else
            {
                Integer after = paginationFilterBean.getCursor() + paginationFilterBean.getNumber() - 1;
                String nextUri = uriInfo
                    .getBaseUriBuilder()
                    .path(UserResource.class)
                    .queryParam(CURSOR, paginationFilterBean.getCursor() + paginationFilterBean.getNumber())
                    .queryParam(NUMBER, paginationFilterBean.getNumber())
                    .build().toString();
                cursor = new Cursor(after, nextUri);
            }

            usersRepresentation = new UsersRepresentation(users, cursor);
        }
        else
        {
            ArrayList<User> users = UserService.getAllUsers();
            for(User user : users)
            {
                UserResource.addSelfAndPosts(uriInfo, user);
            }
            usersRepresentation = new UsersRepresentation(users);
        }

        return Response.ok().entity(usersRepresentation).build();
    }

    @POST
    public Response addUser(@Context UriInfo uriInfo, UserRepresentation userRepresentation)
    {
        User user = UserService.addUser(userRepresentation.getData());
        UserResource.addSelfAndPosts(uriInfo, user);

        return Response.created(getSelfUri(uriInfo, user.getProfileName())).entity(new UserRepresentation(user)).build();
    }

    @PUT
    @Path(USER_ITEM_RESOURCE_PATH)
    public Response updateUser(@Context UriInfo uriInfo, @PathParam(PROFILE_NAME_PATH_PARAM) String profileName, UserRepresentation userRepresentation)
    {
        userRepresentation.getData().setProfileName(profileName);
        User user = UserService.updateUser(userRepresentation.getData());
        UserResource.addSelfAndPosts(uriInfo, user);

        return Response.ok().entity(new UserRepresentation(user)).build();
    }

    @DELETE
    @Path(USER_ITEM_RESOURCE_PATH)
    public Response removeUser(@Context UriInfo uriInfo, @PathParam(PROFILE_NAME_PATH_PARAM) String profileName)
    {
        UserService.removeUser(profileName);
        return Response.noContent().build();
    }

    @Path(POST_COLLECTION_RESOURCE_PATH)
    public PostResource getPostResource()
    {
        return new PostResource();
    }

    private static User addSelfAndPosts(UriInfo uriInfo, User user)
    {
        String selfUri = getSelfUri(uriInfo, user.getProfileName()).toString();
        String postsUri = getPostsUri(uriInfo, user.getProfileName()).toString();
        user.addSelfAndPosts(selfUri, postsUri);

        return user;
    }

    private static URI getSelfUri(UriInfo uriInfo, String profileName)
    {
        return uriInfo
                .getBaseUriBuilder()
                .path(UserResource.class)
                .path(profileName)
                .build();
    }

    private static URI getPostsUri(UriInfo uriInfo, String profileName)
    {
        return uriInfo
                .getBaseUriBuilder()
                .path(UserResource.class)
                .path(UserResource.class, "getPostResource")
                .resolveTemplate(PROFILE_NAME_PATH_PARAM, profileName)
                .build();
    }
}