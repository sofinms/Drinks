using System.Threading;
using System.Threading.Tasks;

namespace Project.API.Domain.Orders
{
    public interface IOrdersRepository
    {
        Task<Order> Save(Order order, CancellationToken token = default(CancellationToken));

        Task<Order> OrderWithId(int orderId, CancellationToken token = default(CancellationToken));
    }
}