using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Project.API.Ordering.Application.DrinkDetails;
using Project.API.Ordering.Application.OrderDetails;
using Project.API.Ordering.Application.OrderService;
using Project.API.Ordering.Domain.Drinks;
using Project.API.Ordering.Domain.Orders;
using Project.API.Infrastructure.Repositories;
using Project.API.WebApi.Swagger;
using Project.API.Servicing.Application.BookedOrders;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.Extensions.Configuration;
using Project.API.Infrastructure.Notifications;
using Project.API.WebApi.MediatR;
using Project.API.Infrastructure.Firebase;
using Project.API.Servicing.Application.FinishOrder;

[assembly: ApiController]
namespace Project.API.WebApi
{
    public class Startup
    {
        private static readonly string CorsAllowAllPolicy = "AllowAll";

        public Startup(IConfiguration config) => Configuration = config;

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        // For more information on how to configure your application, visit https://go.microsoft.com/fwlink/?LinkID=398940
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddMvcCore()
                .AddApiExplorer();

            services.AddCors(options =>
            {
                options.AddPolicy(CorsAllowAllPolicy, builder =>
                {
                    builder
                        .AllowAnyOrigin()
                        .AllowAnyHeader()
                        .AllowAnyMethod();
                });
            });

            services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
                .AddJwtBearer(options =>
                {
                    options.Authority = Configuration["Oidc:Authority"];
                    options.TokenValidationParameters = new Microsoft.IdentityModel.Tokens.TokenValidationParameters
                    {
                        ValidateIssuer = true,
                        ValidIssuer = Configuration["Oidc:Issuer"],
                        ValidateAudience = true,
                        ValidAudience = Configuration["Oidc:Audience"],
                        ValidateLifetime = true
                    };
                });

            services.AddAuthorization();

            services.AddCustomSwagger();
            services.AddCustomMediatR();

            services.AddSingleton<InMemoryDrinksRepository>();
            services.AddSingleton<IDrinksRepository>(provider => provider.GetRequiredService<InMemoryDrinksRepository>());
            services.AddSingleton<IDrinkSizesRepository>(provider => provider.GetRequiredService<InMemoryDrinksRepository>());
            services.AddSingleton<IDrinkDetailsRepository>(provider => provider.GetRequiredService<InMemoryDrinksRepository>());
            services.AddSingleton<IAddInsRepository, InMemoryAddInsRepository>();
            services.AddSingleton<InMemoryOrdersRepository>();
            services.AddSingleton<IOrderDetailsRepository>(provider => provider.GetRequiredService<InMemoryOrdersRepository>());
            services.AddSingleton<IOrdersRepository>(provider => provider.GetRequiredService<InMemoryOrdersRepository>());
            services.AddSingleton<IBookedOrdersRepository>(provider => provider.GetRequiredService<InMemoryOrdersRepository>());
            services.AddSingleton<CreateOrderService>();
            services.AddSingleton<OrderNumberProvider>();
            services.AddSingleton<OrderDraftFactory>();
            services.AddSingleton<OrderFactory>();
            services.AddSingleton<FinishOrderService>();
            services.AddSingleton<NotifiableOrders>();
            services.AddFirebaseNotifications();
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(
            IApplicationBuilder app,
            IWebHostEnvironment env,
            ILogger<Startup> logger
        )
        {
            if (env.IsDevelopment())
            {
                app.UseExceptionHandler("/error-dev");
            }

            app.UseRouting();

            app.UseAuthentication();
            app.UseAuthorization();

            if (env.IsDevelopment())
            {
                logger.LogInformation("CORS: allowing all requests");
                app.UseCors(CorsAllowAllPolicy);
            }
            else
            {
                app.UseCors();
            }

            app.UseCustomSwagger();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapControllers();
            });
        }
    }
}
